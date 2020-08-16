package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HandlerExecutorTest {

    @Test
    void test() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        boolean assignableFrom = Long.class.isAssignableFrom(Number.class);
        System.out.println(assignableFrom);
        System.out.println(Long.class.getSuperclass());
        String a = "1";
        System.out.println(Arrays.toString(Long.class.getMethods()));
        Object valueOf = Long.class.getMethod("valueOf", String.class).invoke(null, a);
        System.out.println(valueOf);

        System.out.println(Arrays.toString(Long.class.getDeclaredMethods()));
    }

}