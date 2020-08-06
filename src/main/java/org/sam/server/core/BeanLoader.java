package org.sam.server.core;

import org.sam.server.HttpServer;
import org.sam.server.annotation.handle.Handler;
import org.sam.server.common.ServerProperties;

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
                .filter(clazz -> clazz.getDeclaredAnnotation(Handler.class) != null)
                .collect(Collectors.toList());
    }

    private static void loadClasses() {
        ClassLoader classLoader = BeanLoader.class.getClassLoader();
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
            loadHandler(classes);
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
        return handlerClasses;
    }


}
