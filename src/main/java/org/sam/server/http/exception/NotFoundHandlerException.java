package org.sam.server.http.exception;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 4:53 PM
 */
public class NotFoundHandlerException extends RuntimeException {
    public NotFoundHandlerException() {
        super("not found handler");
    }
}
