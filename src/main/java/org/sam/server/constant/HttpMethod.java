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
        public boolean hasBody() {
            return true;
        }
    },
    PUT {
        @Override
        public boolean hasBody() {
            return true;
        }
    },
    PATCH {
        @Override
        public boolean hasBody() {
            return true;
        }
    },
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT;

    /**
     * HTTP 바디에 메시지가 존재하는 지 확인합니다.
     *
     * @return HTTP 바디에 메시지가 존재하는지 여부
     */
    public boolean hasBody() {
        return false;
    }
}
