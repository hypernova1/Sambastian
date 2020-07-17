package org.sam.api;

import org.sam.server.http.RequestReceiver;

import java.net.Socket;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class DefaultRequestReceiver extends RequestReceiver {
    public DefaultRequestReceiver(Socket connect) {
        super(connect);
    }
}
