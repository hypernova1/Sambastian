package org.sam.server.context;

import org.sam.server.annotation.ComponentScan;
import org.sam.server.annotation.component.Component;
import org.sam.server.annotation.component.Handler;
import org.sam.server.annotation.handle.RequestMapping;
import org.sam.server.common.ServerProperties;
import org.sam.server.http.Interceptor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * 루트 패키지로 부터 클래스 파일을 읽어 클래스 정보를 저장하는 클래스
 *
 * @author hypernova1
 */
public class BeanClassLoader {
    private String rootPackageName;
    private final List<Class<?>> handlerClasses = new ArrayList<>();
    private final List<Class<?>> componentClasses = new ArrayList<>();
    private final List<Class<?>> interceptorClasses = new ArrayList<>();

    private BeanClassLoader() {
        this.rootPackageName = ServerProperties.get("root-package-name");
        if (this.rootPackageName == null) {
            findRootPackageName();
        }
        loadClasses();
    }

    /**
     * 루트 패키지부터 경로를 탐색하며 핸들러, 컴포넌트, 인터셉터 클래스를 저장한다.
     */
    private void loadClasses() {
        this.rootPackageName = ServerProperties.get("root-package-name");
        String path = rootPackageName.replace(".", "/");
        try {
            List<Class<?>> classes = new ArrayList<>();

            //TODO: 현재는 class path 기준으로 ide 실행인지, jar 실행인지 구분하고 있음 추후 다른 안전한 방식으로 변경해야함
            if (System.getProperty("java.class.path").startsWith("target/")) {
                retrieveFromJar(path, classes);
            } else {
                retrieveFromPath(path, classes);
            }

            this.loadHandlerClasses(classes);
            this.loadComponentClasses(classes);
            this.loadInterceptorClasses(classes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 경로를 기준으로 클래스를 탐색한다.
     *
     * @param path 경로
     * @param classes 찾은 클래스를 담을 컬렉션
     * */
    private void retrieveFromPath(String path, List<Class<?>> classes) throws IOException {
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File directory = new File(resource.getFile());
            classes.addAll(createClasses(directory, rootPackageName));
        }
    }

    /**
     * Jar 내의 클래스를 찾는다.
     *
     * @param path 경로
     * @param classes 찾은 클래스를 담을 컬렉션
     * */
    private static void retrieveFromJar(String path, List<Class<?>> classes) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(System.getProperty("java.class.path"))));
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (!entry.getName().startsWith(path)) {
                continue;
            }

            if (entry.getName().endsWith(".class")) {
                try {
                    String className = entry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 핸들러 클래스를 저장한다.
     *
     * @param classes 클래스 목록
     * @see RequestMapping
     */
    private void loadHandlerClasses(List<Class<?>> classes) {
        this.handlerClasses.addAll(classes.stream()
                .filter(this::isHandlerClass)
                .collect(Collectors.toList()));
    }

    /**
     * 컴포넌트 클래스를 저장한다.
     *
     * @param classes 클래스 목록
     * @see org.sam.server.annotation.component.Component
     */
    private void loadComponentClasses(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
            this.addComponentClass(clazz, declaredAnnotations);
        }
    }

    /**
     * 싱글턴으로 생성할 클래스를 추가한다.
     *
     * @param clazz 클래스
     * @param declaredAnnotations 선언된 어노테이션 목록
     * */
    private void addComponentClass(Class<?> clazz, Annotation[] declaredAnnotations) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (!isComponentClass(declaredAnnotation)) continue;
            this.componentClasses.add(clazz);
        }
    }

    /**
     * 인터셉터를 상속받은 클래스를 저장한다.
     *
     * @param classes 클래스 목록
     * @see org.sam.server.http.Interceptor
     */
    private void loadInterceptorClasses(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (!isInterceptorClass(clazz)) continue;
            this.interceptorClasses.add(clazz);
        }
    }

    /**
     * 디렉토리를 탐색하며 클래스를 찾아 목록을 반환한다.
     *
     * @param directory   디렉토리
     * @param packageName 패키지명
     * @return 해당 디렉토리의 클래스 목록
     */
    private Collection<? extends Class<?>> createClasses(File directory, String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) return classes;
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                Collection<? extends Class<?>> foundClasses = createClasses(file, packageName + "." + file.getName());
                classes.addAll(foundClasses);
                continue;
            }
            Class<?> clazz = this.createClass(packageName, file);
            if (clazz == null) continue;
            classes.add(clazz);
        }
        return classes;
    }

    /**
     * 패키지 안에 있는 클래스를 반환한다.
     *
     * @param packageName 패키지 이름
     * @param file        패키지 안의 파일
     * @return 클래스
     */
    private Class<?> createClass(String packageName, File file) {
        if (!isClassFile(file)) return null;
        try {
            String filePath = this.getFilePath(packageName, file);
            return Class.forName(filePath);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 루트 패키지를 찾는다.
     */
    private void findRootPackageName() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                retrieveFile(new File(resource.getFile()), "");
                if (this.rootPackageName != null) return;
            }
        } catch (IOException e) {
            throw new ComponentScanNotFoundException();
        }
    }

    /**
     * 모든 디렉토리를 탐색하여 루트 패키지를 찾아 저장한다.
     *
     * @param directory   디렉토리
     * @param packageName 패키지 이름
     */
    private void retrieveFile(File directory, String packageName) {
        if (!directory.exists()) return;
        File[] files = directory.listFiles();
        if (files == null) return;
        for (File file : files) {
            this.appendPackageName(file, packageName);
        }
    }

    /**
     * 패키지 이름을 세팅한다.
     *
     * @param file        파일
     * @param packageName 현재까지 만들어진 패키지명
     */
    private void appendPackageName(File file, String packageName) {
        StringBuilder sb = new StringBuilder(packageName);

        if (!sb.toString().isEmpty()) {
            sb.append(".");
        }

        if (isClassFile(file)) {
            try {
                Class<?> clazz = Class.forName(getClassName(sb + file.getName()));
                if (!this.isDeclaredComponentScan(clazz)) return;
                this.rootPackageName = packageName;
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        retrieveFile(file, sb + file.getName());
    }

    /**
     * 핸들러 클래스 목록을 반환한다.
     *
     * @return 핸들러 클래스 목록
     */
    List<Class<?>> getHandlerClasses() {
        return this.handlerClasses;
    }

    /**
     * 컴포넌트 클래스 목록을 반환한다.
     *
     * @return 컴포넌트 클래스 목록
     */
    List<Class<?>> getComponentClasses() {
        return this.componentClasses;
    }

    /**
     * 인터셉터 구현 클래스 목록을 반환한다.
     *
     * @return 인터셉터 구현 클래스 목록
     */
    List<Class<?>> getInterceptorClasses() {
        return this.interceptorClasses;
    }

    /**
     * 클래스의 이름을 반환한다.
     *
     * @param packageName 패키지명
     * @param file        클래스 파일
     * @return 클래스 이름
     */
    private String getFilePath(String packageName, File file) {
        return packageName + "." + getClassName(file.getName());
    }

    /**
     * 클래스의 이름을 반환한다.
     *
     * @param fileName 패키지명
     * @return 클래스 이름
     */
    private String getClassName(String fileName) {
        return fileName.substring(0, fileName.length() - 6);
    }

    /**
     * 해당 클래스가 핸들러 클래스인지 확인한다.
     *
     * @param clazz 클래스 타입
     * @return 핸들러 클래스 여부
     */
    private boolean isHandlerClass(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(Handler.class) != null;
    }

    /**
     * 해당 클래스에 ComponentScan 어노테이션이 선언되어 있는지 확인한다.
     *
     * @param clazz 클래스 타입
     * @return ComponentScan 클래스 여부
     * @see org.sam.server.annotation.ComponentScan
     */
    private boolean isDeclaredComponentScan(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(ComponentScan.class) != null;
    }

    /**
     * 해당 파일이 클래스 파일인지 확인한다.
     *
     * @param file 파일
     * @return 클래스 파일 여부
     */
    private boolean isClassFile(File file) {
        return file.getName().endsWith(".class");
    }

    /**
     * Interceptor를 구현한 클래스인지 확인한다.
     *
     * @param clazz 클래스 타입
     * @return Interceptor 구현 여부
     */
    private boolean isInterceptorClass(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        return Arrays.asList(interfaces).contains(Interceptor.class);
    }

    /**
     * Component 관련 어노테이션인지 확인한다.
     *
     * @param annotation 어노테이션
     * @return Component 어노테이션 여부
     */
    private boolean isComponentClass(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(Component.class) != null || annotation.annotationType().equals(Component.class);
    }

    private static class BeanClassLoaderHolder {
        public static final BeanClassLoader INSTANCE = new BeanClassLoader();
    }

    public static BeanClassLoader getInstance() {
        return BeanClassLoaderHolder.INSTANCE;
    }

}
