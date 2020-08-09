package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/08/10
 * Time: 12:15 AM
 */
public class BeanNotFoundException extends RuntimeException {
    public BeanNotFoundException(String message) {
        super(message);
    }
}
