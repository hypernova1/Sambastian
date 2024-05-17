package org.sam.server.context;

public class CircularReferenceException extends RuntimeException {
    public CircularReferenceException(String message) {
        super("To resolve the circular reference.\n" +  message);
    }
}
