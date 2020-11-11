package org.sam.server.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BeanInfoFactoryTest {

    interface A {}

    class B implements A {}

    @Test
    void get_interface() {
        System.out.println(Arrays.toString(B.class.getInterfaces()));
    }

}