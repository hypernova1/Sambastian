package org.sam.server.http.context;

import org.sam.server.constant.HttpStatus;

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
