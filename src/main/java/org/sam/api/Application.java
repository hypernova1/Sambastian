package org.sam.api;

import org.sam.server.HttpServer;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 20:24 PM
 */
public class Application {
    public static void main(String[] args) {
        HttpServer.start(Application.class);
    }
}
