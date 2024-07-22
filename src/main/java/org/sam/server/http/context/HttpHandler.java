package org.sam.server.http.context;

import org.sam.server.context.BeanContainer;
import org.sam.server.http.SessionManager;
import org.sam.server.http.web.request.HttpRequest;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.HttpResponse;
import org.sam.server.http.web.response.Response;

import java.io.IOException;
import java.net.Socket;

public class HttpHandler implements Runnable {
    private final Socket connect;

    public HttpHandler(Socket connect) {
        this.connect = connect;
    }

    @Override
    public void run() {
        try {
            Request request = HttpRequest.from(connect.getInputStream());
            Response response = HttpResponse.of(connect.getOutputStream(), request.getUrl(), request.getMethod());

            SessionManager.removeExpiredSession();
            if (StaticResourceHandler.isStaticResourceRequest(request, response)) {
                return;
            }

            HttpLauncher.execute(request, response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                connect.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
