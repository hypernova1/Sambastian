package org.sam.server.core;

import org.sam.server.annotation.Component;
import org.sam.server.annotation.Service;
import org.sam.server.annotation.handle.Handler;
import org.sam.server.common.ServerProperties;
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

    private static final String rootPackageName = ServerProperties.get("root-package");

    private static final List<Class<?>> handlerClasses = new ArrayList<>();
    private static final List<Class<?>> componentClasses = new ArrayList<>();
    private static final List<Class<?>> interceptorClasses = new ArrayList<>();

    static {
        loadClasses();
    }

    private static void loadClasses() {
        ClassLoader classLoader = BeanClassLoader.class.getClassLoader();
        String path = rootPackageName.replace(".", "/");
        try {
            Enumeration<URL> resources = classLoader.getResources(path);
            List<File> dir = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dir.add(new File(resource.getFile()));
            }
            List<Class<?>> classes = new ArrayList<>();
            for (File directory : dir) {
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
