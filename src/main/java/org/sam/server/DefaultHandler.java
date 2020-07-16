package org.sam.server;

import java.net.Socket;

public class DefaultHandler extends Handler {
    public DefaultHandler(Socket connect) {
        super(connect);
    }
}
