package org.sam.server.http.context;

import org.sam.server.http.SessionManager;
import org.sam.server.http.web.request.HttpRequest;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.HttpResponse;
import org.sam.server.http.web.response.Response;

import java.io.IOException;
import java.net.Socket;

public class HttpRequestHandler implements Runnable {
    private final Socket clientSocket;
    private final HttpRequestDispatcherLauncher httpLauncher = HttpRequestDispatcherLauncher.getInstance();

    public HttpRequestHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {
            SessionManager.removeExpiredSession();
            Request request = HttpRequest.from(connect.getInputStream());
            if (request == null) {
                return;
            }
            Response response = HttpResponse.of(connect.getOutputStream(), request.getUrl(), request.getMethod());
            if (StaticResourceHandler.isStaticResourceRequest(request, response)) {
                return;
            }

            httpLauncher.execute(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
