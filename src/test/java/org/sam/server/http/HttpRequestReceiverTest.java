package org.sam.server.http;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 3:47 PM
 */
class HttpRequestReceiverTest {

    @Test
    void test() {
        String handlerPath = "/";
        String requestPath = "/path";

        assertEquals(true, requestPath.startsWith(handlerPath));
    }

}