package org.sam.server.core;

import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.HttpRequest;
import org.sam.server.http.HttpResponse;
import org.sam.server.http.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
            HttpRequest httpRequest = Request.create(connect.getInputStream());
            HttpResponse httpResponse = HttpResponse.create(connect.getOutputStream(), httpRequest.getPath());
            try {
                HandlerInfo handlerInfo = new HandlerFinder(httpRequest, httpResponse).findHandlerMethod();
                HandlerExecutor handlerExecutor = new HandlerExecutor(httpRequest, httpResponse, handlerInfo);
                handlerExecutor.execute();
            } catch (NotFoundHandlerException e) {
                httpResponse.fileNotFound();
                throw new IOException(e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
