package org.sam.server.http;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {

    @Test
    void test() {
        String property = System.getProperty("user.dir");
        property += "/src/main/resources/static/404.html";

        System.out.println(property);

        File file = new File(property);

        System.out.println(file.exists());
    }

}