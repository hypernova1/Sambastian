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
            Request request = HttpRequest.from(clientSocket.getInputStream());
            Response response = HttpResponse.of(clientSocket.getOutputStream(), request.getUrl(), request.getMethod());

            SessionManager.removeExpiredSession();
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
