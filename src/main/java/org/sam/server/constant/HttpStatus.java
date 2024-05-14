package org.sam.server.constant;

/**
 * HTTP 응답 상태 상수
 *
 * @author hypernova1
 */
public enum HttpStatus {

    OK("200", "OK"),
    CREATED("201", "Created"),
    BAD_REQUEST("400", "Bad Request") {
        @Override
        public boolean isError() {
            return true;
        }
    },
    UNAUTHORIZED("401", "Unauthorized") {
        @Override
        public boolean isError() {
            return true;
        }
    },
    FORBIDDEN("403", "Forbidden") {
        @Override
        public boolean isError() {
            return true;
        }
    },
    NOT_FOUND("404", "Not Found") {
        @Override
        public boolean isError() {
            return true;
        }
    },
    METHOD_NOT_ALLOWED("405", "Method Not Allowed") {
        @Override
        public boolean isError() {
            return true;
        }
    },
    NOT_IMPLEMENTED("501", "Not Implemented") {
        @Override
        public boolean isError() {
            return true;
        }
    };

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
        return false;
    }

}
