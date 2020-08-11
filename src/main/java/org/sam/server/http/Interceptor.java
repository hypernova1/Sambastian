package org.sam.server.http;

public interface Interceptor {

    void preHandler(Request request, Response response);

    void postHandler(Request request, Response response);

}
