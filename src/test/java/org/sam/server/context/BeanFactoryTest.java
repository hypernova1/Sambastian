package org.sam.server.context;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by melchor
 * Date: 2020/08/29
 * Time: 8:27 PM
 */
class BeanFactoryTest {

    interface A {}

    class B implements A {}

    @Test
    void get_interface() {
        System.out.println(Arrays.toString(B.class.getInterfaces()));
    }

}