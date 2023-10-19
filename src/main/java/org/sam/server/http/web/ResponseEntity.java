package org.sam.server.http.web;

import org.sam.server.constant.HttpStatus;

/**
 * 응답 상태 및 데이터를 포장하는 클래스입니다. 핸들러에서 반환 값으로 사용할 수 있습니다.
 *
 * @author hypernova1
 */
public class ResponseEntity<T> {

    private final HttpStatus httpStatus;
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
