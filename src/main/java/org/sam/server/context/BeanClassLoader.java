package org.sam.server.context;

import org.sam.server.annotation.component.Component;
import org.sam.server.annotation.ComponentScan;
import org.sam.server.annotation.component.Repository;
import org.sam.server.annotation.component.Service;
import org.sam.server.annotation.component.Handler;
import org.sam.server.annotation.handle.RequestMapping;
import org.sam.server.exception.ComponentScanNotFoundException;
import org.sam.server.http.Interceptor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 루트 패키지로 부터 클래스 파일을 읽어 클래스 정보를 저장하는 클래스입니다.
 *
 * @author hypernova1
 */
public class BeanClassLoader {

    private static String rootPackageName;

    private static final List<Class<?>> handlerClasses = new ArrayList<>();

    private static final List<Class<?>> componentClasses = new ArrayList<>();

    private static final List<Class<?>> interceptorClasses = new ArrayList<>();

    static {
        findRootPackageName();
        loadClasses();
    }

    /**
     * 루트 패키지부터 경로를 탐색하며 핸들러, 컴포넌트, 인터셉터 클래스를 저장합니다.
     * */
    private static void loadClasses() throws ComponentScanNotFoundException {
        if (rootPackageName == null) throw new ComponentScanNotFoundException();
        String path = rootPackageName.replace(".", "/");
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<Class<?>> classes = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                classes.addAll(createClasses(directory, rootPackageName));
            }
            loadHandlerClasses(classes);
            loadComponentClasses(classes);
            loadInterceptorClasses(classes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 핸들러 클래스를 저장합니다.
     *
     * @param classes 클래스 목록
     * @see RequestMapping
     * */
    private static void loadHandlerClasses(List<Class<?>> classes) {
        handlerClasses.addAll(classes.stream()
                .filter(BeanClassLoader::isHandlerClass)
                .collect(Collectors.toList()));
    }

    /**
     * 컴포넌트 클래스를 저장합니다.
     *
     * @param classes 클래스 목록
     * @see org.sam.server.annotation.component.Component
     * */
    private static void loadComponentClasses(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
            addComponentClass(clazz, declaredAnnotations);
        }
    }

    private static void addComponentClass(Class<?> clazz, Annotation[] declaredAnnotations) {
        for (Annotation declaredAnnotation : declaredAnnotations) {
            if (!isComponentClass(declaredAnnotation)) continue;
            componentClasses.add(clazz);
        }
    }

    /**
     * 인터셉터를 상속받은 클래스를 저장합니다.
     *
     * @param classes 클래스 목록
     * @see org.sam.server.http.Interceptor
     * */
    private static void loadInterceptorClasses(List<Class<?>> classes) {
        for (Class<?> clazz : classes) {
            if (!isInterceptorClass(clazz)) continue;
            interceptorClasses.add(clazz);
        }
    }

    /**
     * 디렉토리를 탐색하며 클래스를 찾아 목록을 반환합니다.
     *
     * @param directory 디렉토리
     * @param packageName 패키지명
     * @return 해당 디렉토리의 클래스 목록
     * */
    private static Collection<? extends Class<?>> createClasses(File directory, String packageName) {
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
            Class<?> clazz = createClass(packageName, file);
            if (clazz == null) continue;
            classes.add(clazz);
        }
        return classes;
    }

    /**
     * 패키지 안에 있는 클래스를 반환합니다.
     *
     * @param packageName 패키지 이름
     * @param file 패키지 안의 파일
     * @return 클래스
     * */
    private static Class<?> createClass(String packageName, File file) {
        if (!isClassFile(file)) return null;
        try {
            String fullClassName = getClassName(packageName, file);
            return Class.forName(fullClassName);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 루트 패키지를 찾습니다.
     * */
    private static void findRootPackageName() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> resources = classLoader.getResources("");
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                boolean existComponentScan = findRootPackageName(new File(resource.getFile()), "");
                if (existComponentScan) break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 모든 디렉토리를 탐색하여 루트 패키지를 찾아 저장합니다.
     *
     * @param directory 디렉토리
     * @param packageName 패키지 이름
     * @return  찾았는 지에 대한 여부
     * */
    private static boolean findRootPackageName(File directory, String packageName) {
        if (!directory.exists()) return false;
        File[] files = directory.listFiles();
        if (files == null) return false;
        for (File file : files) {
            StringBuilder packageNameBuilder = new StringBuilder(packageName);
            if (file.isDirectory()) {
                if (packageNameBuilder.length() > 0) packageNameBuilder.append(".");
                findRootPackageName(file, packageNameBuilder + file.getName());
            } else if (isClassFile(file)) {
                String fileName = packageNameBuilder + "." + file.getName();
                try {
                    Class<?> clazz = Class.forName(getClassName(fileName));
                    if (!isComponentScanClass(clazz)) continue;

                    rootPackageName = packageName;
                    return true;
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 핸들러 클래스 목록을 반환합니다.
     *
     * @return 핸들러 클래스 목록
     * */
    static List<Class<?>> getHandlerClasses() {
        return handlerClasses;
    }

    /**
     * 컴포넌트 클래스 목록을 반환합니다.
     *
     * @return 컴포넌트 클래스 목록
     * */
    static List<Class<?>> getComponentClasses() {
        return componentClasses;
    }

    /**
     * 인터셉터 구현 클래스 목록을 반환합니다.
     *
     * @return 인터셉터 구현 클래스 목록
     * */
    static List<Class<?>> getInterceptorClasses() {
        return interceptorClasses;
    }

    /**
     * 클래스의 이름을 반환합니다.
     *
     * @param packageName 패키지명
     * @param file 클래스 파일
     * @return 클래스 이름
     * */
    private static String getClassName(String packageName, File file) {
        return packageName + "." + getClassName(file.getName());
    }

    /**
     * 클래스의 이름을 반환합니다.
     *
     * @param fileName 패키지명
     * @return 클래스 이름
     * */
    private static String getClassName(String fileName) {
        return fileName.substring(0, fileName.length() - 6);
    }

    /**
     * 해당 클래스가 핸들러 클래스인지 확인합니다.
     *
     * @param clazz 클래스 타입
     * @return 핸들러 클래스 여부
     * */
    private static boolean isHandlerClass(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(Handler.class) != null;
    }

    /**
     * 해당 클래스에 ComponentScan 어노테이션이 붙어 있는지 확인합니다.
     *
     * @param clazz 클래스 타입
     * @return ComponentScan 클래스 여부
     * @see org.sam.server.annotation.ComponentScan
     * */
    private static boolean isComponentScanClass(Class<?> clazz) {
        return clazz.getDeclaredAnnotation(ComponentScan.class) != null;
    }

    /**
     * 해당 파일이 클래스 파일인지 확인합니다.
     *
     * @param file 파일
     * @return 클래스 파일 여부
     * */
    private static boolean isClassFile(File file) {
        return file.getName().endsWith(".class");
    }

    /**
     * Interceptor를 구현한 클래스인지 확인합니다.
     *
     * @param clazz 클래스 타입
     * @return Interceptor 구현 여부
     * */
    private static boolean isInterceptorClass(Class<?> clazz) {
        Class<?>[] interfaces = clazz.getInterfaces();
        return Arrays.asList(interfaces).contains(Interceptor.class);
    }

    /**
     * Component 관련 어노테이션인지 확인합니다.
     *
     * @param annotation 어노테이션
     * @return Component 어노테이션 여부
     * */
    private static boolean isComponentClass(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(Component.class) != null || annotation.annotationType().equals(Component.class);
    }

}
