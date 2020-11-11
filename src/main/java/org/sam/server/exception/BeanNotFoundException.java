package org.sam.server.exception;

public class BeanNotFoundException extends RuntimeException {
    public BeanNotFoundException(String message) {
        super(message + " bean is not found");
    }
}
