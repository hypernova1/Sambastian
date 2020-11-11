package org.sam.server.exception;

public class BeanAccessModifierException extends RuntimeException {
    public BeanAccessModifierException() {
        super("access modifier of bean's method must be public.");
    }
}
