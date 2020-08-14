package org.sam.server;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @Test
    void test() throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        String packageName = "org.sam.server";
        assert classLoader != null;
        String path = packageName.replace(".", "/");
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
            classes.addAll(findClasses(directory, packageName));
        }

        classes.forEach(clz -> System.out.println(clz.getSimpleName()));
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

    @Test
    void test2() {
        System.out.println(System.getenv("PORT"));
    }

}