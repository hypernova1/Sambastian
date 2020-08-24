package org.sam.server.http;

import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpLauncher {

    static void execute(Socket connect) {
        try {
            HttpRequest httpRequest = Request.create(connect.getInputStream());
            if (httpRequest == null) return;
            HttpResponse httpResponse = HttpResponse.create(connect.getOutputStream(), httpRequest.getPath(), httpRequest.getMethod());
            findHandler(httpRequest, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void findHandler(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        try {
            if (httpRequest.getPath().equals("/favicon.ico")) {
                httpResponse.getFavicon();
                return;
            }
            if (httpRequest.getPath().startsWith("/resources")) {
                httpResponse.getStaticResources();
                return;
            }
            HandlerInfo handlerInfo = HandlerFinder.of(httpRequest, httpResponse).createHandlerInfo();
            if (handlerInfo == null) return;
            HandlerExecutor.of(httpRequest, httpResponse, handlerInfo).execute();
        } catch (HandlerNotFoundException e) {
            httpResponse.notFound();
            throw new IOException(e);
        }
    }
}
