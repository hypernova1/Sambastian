package org.sam.server.http.context;

import org.sam.server.constant.HttpStatus;

/**
 * HTTP 예외 클래스 해당 클래스를 던지면 내부에 선언된 상태 및 메시지를 응답한다.
 * */
public class HttpException extends RuntimeException {
    private final HttpStatus status;
    private final String message;

    public HttpException(HttpStatus status) {
        this(status, null);
    }

    public HttpException(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

}
