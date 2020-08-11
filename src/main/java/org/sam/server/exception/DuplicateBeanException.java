package org.sam.server.exception;

/**
 * Created by melchor
 * Date: 2020/08/11
 * Time: 9:43 PM
 */
public class DuplicateBeanException extends RuntimeException {
    public DuplicateBeanException(String name) {
        super(name + " is duplicated");
    }
}
