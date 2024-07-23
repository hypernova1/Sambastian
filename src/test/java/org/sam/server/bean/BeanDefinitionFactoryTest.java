package org.sam.server.bean;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

class BeanDefinitionFactoryTest {

    interface A {}

    class B implements A {}

    @Test
    void get_interface() {
        System.out.println(Arrays.toString(B.class.getInterfaces()));
    }

}