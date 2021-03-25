package org.sam.server.context;

import org.junit.jupiter.api.Test;
import org.sam.server.http.context.HttpServer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

class BeanInfoClassLoaderTest {

    static class TestBean {

        public String foo() {
            return "";
        }
    }

    @Test
    void test() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Constructor<?>[] constructors = TestBean.class.getConstructors();
        System.out.println(constructors.length);
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameterTypes = constructor.getParameters();
            for (Parameter parameterType : parameterTypes) {
                System.out.println(parameterType.getName());
            }


            Method[] declaredMethods = TestBean.class.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                System.out.println(declaredMethod.getName());
            }
        }

        TestBean testBean = TestBean.class.getDeclaredConstructor(null).newInstance();
        System.out.println(testBean);

        System.out.println(TestBean.class.getSimpleName());
    }

    @Test
    void test2() {
        Package[] packages = Package.getPackages();
        for (Package p : packages) {
            if (p.getName().equals("org.sam.server.context")) {
            }
        }
    }

}