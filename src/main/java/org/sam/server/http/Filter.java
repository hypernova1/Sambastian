package org.sam.server.http;

import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

public interface Filter {

    void init();

    void doFilter(Request request, Response response, FilterChain chain);

    void destroy();

}
