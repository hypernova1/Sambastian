package org.sam.server;

import org.sam.server.annotation.handle.GetHandle;
import org.sam.server.annotation.handle.Handler;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 2:33 PM
 */
@Handler("/path")
public class TestHandler {

    @GetHandle("/test")
    public void testMethod() {
        System.out.println(1111);
    }
}
