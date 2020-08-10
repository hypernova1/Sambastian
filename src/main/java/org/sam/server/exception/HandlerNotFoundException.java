package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 4:53 PM
 */
public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException() {
        super("handler not found");
    }
}
