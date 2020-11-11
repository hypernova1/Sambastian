package org.sam.server.context;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BeanContainerTest {

    static interface AA {

    }

    static class A {

    }

    static class B implements AA {

    }

    @Test
    void test() {
        System.out.println(AA.class);
        assertTrue(AA.class.isAssignableFrom(B.class));
    }


}