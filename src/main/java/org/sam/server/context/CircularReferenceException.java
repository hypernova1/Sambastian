package org.sam.server.context;

/**
 * 순환 참조 발생시 발생하는 예외
 * */
public class CircularReferenceException extends RuntimeException {
    public CircularReferenceException(String message) {
        super("To resolve the circular reference.\n" +  message);
    }
}
