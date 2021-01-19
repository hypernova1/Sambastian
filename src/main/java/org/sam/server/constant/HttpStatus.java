package org.sam.server.constant;

/**
 * HTTP 응답 상태에 대한 상수입니다.
 *
 * @author hypernova1
 */
public enum HttpStatus {

    OK("200", "OK"),
    CREATED("201", "Created"),
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
    NOT_IMPLEMENTED("501", "Not Implemented");

    private final String code;
    private final String message;

    HttpStatus(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return this.code;
    }

    public String getMessage() {
        return this.message;
    }

}
