package org.sam.server.core;

import org.apache.log4j.Logger;
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
public class RequestReceiver {

    private static final Logger logger = Logger.getLogger(RequestReceiver.class);

    private final Socket connect;

    public RequestReceiver(Socket connect) {
        this.connect = connect;
    }

    public void analyzeRequest() {
        try {
            HttpRequest httpRequest = Request.create(connect.getInputStream());
            HttpResponse httpResponse = HttpResponse.create(connect.getOutputStream(), httpRequest.getPath());
            findHandler(httpRequest, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findHandler(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        try {
            if (httpRequest.getPath().startsWith("/resources")) {
                httpResponse.getStaticResources();
                return;
            }
            HandlerInfo handlerInfo = new HandlerFinder(httpRequest, httpResponse).createHandlerInfo();
            new HandlerExecutor(httpRequest, httpResponse, handlerInfo).execute();
        } catch (HandlerNotFoundException e) {
            httpResponse.fileNotFound();
            throw new IOException(e);
        }
    }
}
