package org.sam.server.http;

import org.sam.server.constant.HttpStatus;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 9:44 PM
 */
public class ResponseEntity<T> {
    private HttpStatus httpStatus;
    private T value;

    public ResponseEntity(HttpStatus httpStatus, T value) {
        this.httpStatus = httpStatus;
        this.value = value;
    }

    public ResponseEntity(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public static <T> ResponseEntity<T> of(HttpStatus httpStatus, T value) {
        return new ResponseEntity<T>(httpStatus, value);
    }

    public static <T> ResponseEntity<T> ok(T value) {
        return new ResponseEntity<T>(HttpStatus.OK, value);
    }

    public static <T> ResponseEntity<T> notFound(T value) {
        return new ResponseEntity<T>(HttpStatus.NOT_FOUND, value);
    }

    public static <T> ResponseEntity<?> badRequest(T value) {
        return new ResponseEntity<T>(HttpStatus.BAD_REQUEST, value);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public T getValue() {
        return this.value;
    }
}
