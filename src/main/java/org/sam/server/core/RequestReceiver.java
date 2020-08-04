package org.sam.server.core;

import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.HttpRequest;
import org.sam.server.http.HttpResponse;
import org.sam.server.http.Request;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class RequestReceiver {

    private final Socket connect;

    public RequestReceiver(Socket connect) {
        this.connect = connect;
    }

    public void analyzeRequest() {
        try {
            InputStream inputStream = connect.getInputStream();
            OutputStream outputStream = connect.getOutputStream();

            HttpRequest httpRequest = Request.create(inputStream);
            HttpResponse httpResponse = HttpResponse.create(outputStream, httpRequest.getPath());
            try {
                if (httpRequest.getPath().startsWith("/resources")) {
                    httpResponse.getStaticResources();
                } else {
                    HandlerInfo handlerInfo = new HandlerFinder(httpRequest, httpResponse).findHandlerMethod();
                    HandlerExecutor handlerExecutor = new HandlerExecutor(httpRequest, httpResponse, handlerInfo);
                    handlerExecutor.execute();
                }
            } catch (HandlerNotFoundException e) {
                httpResponse.fileNotFound();
                throw new IOException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
