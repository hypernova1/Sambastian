package org.sam.server.http;

public interface Interceptor {

    void beforeHandler(HttpRequest request, HttpResponse response);

    void afterHandler(HttpRequest request, HttpResponse response);

}
