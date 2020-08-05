package org.sam.server.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by melchor
 * Date: 2020/08/05
 * Time: 6:19 PM
 */
class HttpMultipartRequestTest {

    @Test
    void confirm_type() {
        String foo = "foo";
        Object bar = foo;
        Object bar2 = null;
        assertThrows(NullPointerException.class, () -> bar2.getClass());
        assertEquals(String.class, bar.getClass());
    }

}