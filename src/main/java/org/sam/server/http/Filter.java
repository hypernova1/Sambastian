package org.sam.server.http;

import org.sam.server.http.web.Request;
import org.sam.server.http.web.Response;

public interface Filter {

    void init();

    void doFilter(Request request, Response response, FilterChain chain);

    void destroy();

}
