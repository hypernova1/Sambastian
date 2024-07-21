package org.sam.server.http.context;

import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

public class StaticResourceHandler {

    public static boolean isStaticResourceRequest(Request request, Response response) {
        if (request.isFaviconRequest()) {
            response.favicon();
            return true;
        }
        if (request.isResourceRequest()) {
            response.staticResources();
            return true;
        }

        if (request.isRootRequest()) {
            response.indexFile();
            return true;
        }

        if (request.isOptionsRequest()) {
            response.allowedMethods();
            return true;
        }

        return false;
    }

}
