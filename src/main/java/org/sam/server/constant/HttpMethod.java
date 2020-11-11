package org.sam.server.constant;

/**
 * HTTP Method에 대한 상수입니다.
 *
 * @author hypernova1
 */
public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,
    PATCH;

    public static HttpMethod get(String value) {
        HttpMethod method;
        switch (value) {
            case "GET": method = GET; break;
            case "POST": method = POST; break;
            case "PUT": method = PUT; break;
            case "DELETE": method = DELETE; break;
            case "HEAD": method = HEAD; break;
            case "OPTIONS": method = OPTIONS; break;
            case "TRACE": method = TRACE; break;
            case "CONNECT": method = CONNECT; break;
            case "PATCH": method = PATCH; break;
            default: method = null;
        }

        return method;
    }

}
