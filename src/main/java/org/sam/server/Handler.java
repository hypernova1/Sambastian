package org.sam.server;

import org.sam.server.constant.HttpMethod;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Handler {

    private final Socket connect;

    public Handler(Socket connect) {
        this.connect = connect;
    }

    public void requestAnalyze() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            Request request = Request.create(in);
            Response response = Response.create(out, dataOut, request.getPath());

            if (!request.getMethod().equals(HttpMethod.GET) && !request.getMethod().equals(HttpMethod.HEAD)) {
                response.methodNotImplemented();
                return;
            }

            executeHandle(request, response);

            connect.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeHandle(Request request, Response response) throws IOException {
        try {
            switch (request.getMethod()) {
                case GET: {
                    doGet(request, response);
                }
                break;
                case POST: {
                    doPost(request, response);
                }
                break;
                default: break;
            }
            response.execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void doPost(Request request, Response response) throws IOException {
    }

    public void doGet(Request request, Response response) throws IOException {
    }

}
