package org.sam.server.http;

public interface Filter {

    void init();

    void doFilter(Request request, Response response, FilterChain chain);

    void destroy();

}
