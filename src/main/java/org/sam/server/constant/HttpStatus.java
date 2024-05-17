package org.sam.server.constant;

/**
 * HTTP 응답 상태 상수
 *
 * @author hypernova1
 */
public enum HttpStatus {

    OK("200", "OK"),
    CREATED("201", "Created"),
    NO_CONTENT("204", "No Content"),
    BAD_REQUEST("400", "Bad Request"),
    UNAUTHORIZED("401", "Unauthorized"),
    FORBIDDEN("403", "Forbidden"),
    NOT_FOUND("404", "Not Found"),
    METHOD_NOT_ALLOWED("405", "Method Not Allowed"),
    NOT_IMPLEMENTED("501", "Not Implemented"),

    INTERNAL_SERVER_ERROR("500", "Internal Server Error");

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

    public boolean isError() {
        return this.code.startsWith("4") || this.code.startsWith("5");
    }

    public HttpErrorType errorType() {
        if (this.code.startsWith("1") || this.code.startsWith("2") || this.code.startsWith("3")) {
            return HttpErrorType.NONE;
        }

        if (this.code.startsWith("4")) {
            return HttpErrorType.CLIENT;
        }

        return HttpErrorType.SERVER;
    }

}
