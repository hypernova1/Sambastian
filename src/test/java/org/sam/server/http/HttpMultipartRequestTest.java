package org.sam.server.http;

import org.junit.jupiter.api.Test;
import org.sam.server.constant.ContentType;

import static org.junit.jupiter.api.Assertions.*;

class HttpMultipartRequestTest {

    @Test
    void confirm_type() {
        String foo = "foo";
        Object bar = foo;
        Object bar2 = null;
        assertThrows(NullPointerException.class, () -> bar2.getClass());
        assertEquals(String.class, bar.getClass());
    }

    @Test
    void test() {
        ContentType contentType = ContentType.get("text/plain");
        System.out.println(ContentType.TEXT_HTML);
    }

}