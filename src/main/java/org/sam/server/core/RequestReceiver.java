package org.sam.server.core;

import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.Request;
import org.sam.server.http.Response;

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
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream(), StandardCharsets.UTF_8))) {
            Request request = Request.create(in);
            Response response = Response.create(connect.getOutputStream(), request.getPath());
            try {
                HandlerInfo handlerInfo = new HandlerFinder(request, response).findHandlerMethod();
                HandlerExecutor handlerExecutor = new HandlerExecutor(request, response, handlerInfo);
                handlerExecutor.execute();
            } catch (NotFoundHandlerException e) {
                response.fileNotFound();
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
