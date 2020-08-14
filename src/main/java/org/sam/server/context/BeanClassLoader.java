package org.sam.server.context;

import org.sam.server.annotation.component.Component;
import org.sam.server.annotation.ComponentScan;
import org.sam.server.annotation.component.Service;
import org.sam.server.annotation.component.Handler;
import org.sam.server.exception.ComponentScanNotFoundException;
import org.sam.server.http.Interceptor;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:59 PM
 */
public class BeanClassLoader {

    private static String rootPackageName;
    private static final List<Class<?>> handlerClasses = new ArrayList<>();
    private static final List<Class<?>> componentClasses = new ArrayList<>();
    private static final List<Class<?>> interceptorClasses = new ArrayList<>();

    static {
        try {
            loadRootPackageName();
            loadClasses();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadClasses() {
        if (rootPackageName == null) throw new ComponentScanNotFoundException();
        String path = rootPackageName.replace(".", "/");
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<Class<?>> classes = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                classes.addAll(findClasses(directory, rootPackageName));
            }
            loadHandlerClasses(classes);
            loadComponentClasses(classes);
            loadInterceptorClasses(classes);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void loadHandlerClasses(List<Class<?>> classes) {
        handlerClasses.addAll(classes.stream()
                .filter(clazz -> clazz.getDeclaredAnnotation(Handler.class) != null)
                .collect(Collectors.toList()));
    }

    private static void loadComponentClasses(List<Class<?>> classes) {
        List<Class<?>> componentTypes = Arrays.asList(Service.class, Component.class);
        classes.forEach(clazz -> {
            Annotation[] declaredAnnotations = clazz.getDeclaredAnnotations();
            for (Annotation declaredAnnotation : declaredAnnotations) {
                if (componentTypes.contains(declaredAnnotation.annotationType())) {
                    componentClasses.add(clazz);
                }
            }
        });
    }

    private static void loadInterceptorClasses(List<Class<?>> classes) {
        classes.forEach(clazz -> {
            Class<?>[] interfaces = clazz.getInterfaces();
            long count = Arrays.stream(interfaces).filter(interfaze -> interfaze.equals(Interceptor.class)).count();
            if (count > 0) interceptorClasses.add(clazz);
        });
    }

    private static Collection<? extends Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) return classes;
        File[] files = directory.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + "." + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private static void loadRootPackageName() throws IOException, ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Enumeration<URL> resources = classLoader.getResources("");
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            boolean existComponentScan = loadRootPackageName(new File(resource.getFile()), "");
            if (existComponentScan) break;
        }
    }

    private static boolean loadRootPackageName(File directory, String packageName) throws ClassNotFoundException {
        if (!directory.exists()) return false;
        File[] files = directory.listFiles();
        for (File file : files) {
            StringBuilder packageNameBuilder = new StringBuilder(packageName);
            if (file.isDirectory()) {
                if (!packageNameBuilder.toString().equals("")) packageNameBuilder.append(".");
                loadRootPackageName(file, packageNameBuilder + file.getName());
            } else if (file.getName().endsWith(".class")) {
                String fileName = packageNameBuilder + "." + file.getName();
                Class<?> clazz = Class.forName(fileName.substring(0, fileName.length() - 6));
                if (clazz.getDeclaredAnnotation(ComponentScan.class) != null) {
                    rootPackageName = packageName;
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Class<?>> getHandlerClasses() {
        return handlerClasses;
    }

    public static List<Class<?>> getComponentClasses() {
        return componentClasses;
    }

    public static List<Class<?>> getInterceptorClasses() {
        return interceptorClasses;
    }
}
