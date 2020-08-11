package org.sam.server.http;

public interface Interceptor {

    void preHandler(HttpRequest request, HttpResponse response);

    void postHandler(HttpRequest request, HttpResponse response);

}
