package org.sam.server.exception;

public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException() {
        super("handler not found");
    }
}
