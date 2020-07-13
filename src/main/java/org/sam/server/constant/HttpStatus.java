package org.sam.server.constant;

public enum HttpStatus {

    OK("200", "OK"),
    NOT_FOUND("404", "File Not Found"),
    NOT_IMPLEMENTED("501", "Not Implemented");

    private String code;
    private String message;

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
