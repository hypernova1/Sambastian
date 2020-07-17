package org.sam.api.handler;

import org.sam.server.annotation.handle.GetHandle;
import org.sam.server.annotation.handle.Handler;
import org.sam.server.annotation.handle.RestApi;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 2:33 PM
 */
@Handler("/path")
public class TestHandler {

    @GetHandle("/test")
    @RestApi
    public void testMethod(String a, int b) {
        System.out.println("Test Method Handler");
    }
}
