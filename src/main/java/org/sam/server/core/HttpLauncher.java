package org.sam.server.core;

import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.HttpRequest;
import org.sam.server.http.HttpResponse;
import org.sam.server.http.Request;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpLauncher {

    public static void execute(Socket connect) {
        try {
            HttpRequest httpRequest = Request.create(connect.getInputStream());
            if (httpRequest == null) return;
            HttpResponse httpResponse = HttpResponse.create(connect.getOutputStream(), httpRequest.getPath());
            findHandler(httpRequest, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void findHandler(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        try {
            if (httpRequest.getPath().startsWith("/resources")) {
                httpResponse.getStaticResources();
                return;
            }
            HandlerInfo handlerInfo = new HandlerFinder(httpRequest, httpResponse).createHandlerInfo();
            if (handlerInfo == null) return;
            new HandlerExecutor(httpRequest, httpResponse, handlerInfo).execute();
        } catch (HandlerNotFoundException e) {
            httpResponse.fileNotFound();
            throw new IOException(e);
        }
    }
}
