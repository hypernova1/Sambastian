package org.sam.server.core;

import org.apache.log4j.Logger;
import org.sam.server.annotation.Component;
import org.sam.server.annotation.Service;
import org.sam.server.annotation.handle.Handler;
import org.sam.server.common.ServerProperties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:59 PM
 */
public class BeanContainer {

    private static final Logger logger = Logger.getLogger(BeanContainer.class);
    private static final String rootPackageName = ServerProperties.get("root-package");

    private static final List<Class<?>> componentTypes = Arrays.asList(Service.class, Component.class);

    private static final List<Class<?>> handlerBeans = new ArrayList<>();
    private static final Map<String, Class<?>> componentBeans = new HashMap<>();

    static {
        loadClasses();
    }

    public static void loadHandlers(List<Class<?>> classes) {
        handlerBeans.addAll(classes.stream()
                .filter(clazz -> clazz.getDeclaredAnnotation(Handler.class) != null)
                .collect(Collectors.toList()));
    }

    public static void loadComponents(List<Class<?>> classes) {
        classes.forEach(clazz -> {
            if (componentTypes.contains(clazz)) {
                componentBeans.put(clazz.getSimpleName(), clazz);
                logger.info("create bean, bean name: " + clazz.getSimpleName());
            }
        });
    }

    private static void loadClasses() {
        ClassLoader classLoader = BeanContainer.class.getClassLoader();
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
            loadHandlers(classes);
            loadComponents(classes);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
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
        return handlerBeans;
    }

    public static Map<String, Class<?>> getComponentBeans() {
        return componentBeans;
    }


}
