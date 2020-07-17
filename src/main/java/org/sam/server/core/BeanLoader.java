package org.sam.server.core;

import org.sam.server.annotation.handle.Handler;
import org.sam.server.util.ServerProperties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:59 PM
 */
public class BeanLoader {

    private static List<Class<?>> handlerClasses;
    private static String rootPackageName = ServerProperties.get("root-package");

    static {
        loadClasses();
    }

    public static void loadHandler(List<Class<?>> classes) {
        handlerClasses = classes.stream()
                .filter(clazz -> clazz.equals(Handler.class))
                .collect(Collectors.toList());
    }


    private static void loadClasses() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        assert classLoader != null;
        String path = rootPackageName.replace(".", "/");
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<File> dir = new ArrayList<>();
        if (resources != null) {
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dir.add(new File(resource.getFile()));
            }
        }

        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dir) {
            try {
                classes.addAll(findClasses(directory, rootPackageName));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        loadHandler(classes);
    }

    private static Collection<? extends Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) return classes;

        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
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

}
