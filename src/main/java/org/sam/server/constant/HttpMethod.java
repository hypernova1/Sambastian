package org.sam.server.constant;

/**
 * HTTP Method에 대한 상수입니다.
 *
 * @author hypernova1
 */
public enum HttpMethod {
    GET,
    POST {
        @Override
        boolean hasBody() {
            return true;
        }
    },
    PUT {
        @Override
        boolean hasBody() {
            return true;
        }
    },
    PATCH {
        @Override
        boolean hasBody() {
            return true;
        }
    },
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT;

    boolean hasBody() {
        return false;
    }
}
