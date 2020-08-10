package org.sam.server.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by melchor
 * Date: 2020/08/10
 * Time: 9:19 AM
 */
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