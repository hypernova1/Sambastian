package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/08/11
 * Time: 4:36 PM
 */
public class BeanAccessModifierException extends RuntimeException {
    public BeanAccessModifierException() {
        super("access modifier of bean's method must be public.");
    }
}
