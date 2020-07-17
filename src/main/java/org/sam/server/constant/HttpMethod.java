package org.sam.server.constant;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
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
