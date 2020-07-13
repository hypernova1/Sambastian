package org.sam.server;

import org.sam.server.constant.HttpMethod;

import java.io.*;
import java.net.Socket;

public class Handler {

    private Socket connect;

    public Handler(Socket connect) {
        this.connect = connect;

    }

    public void requestAnalyze() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            Request request = Request.create(in);
            Response response = Response.create(out, dataOut, request.getPath());
            if (!request.getMethod().equals(HttpMethod.GET) && !request.getMethod().equals(HttpMethod.HEAD)) {
                response.methodNotImplemented();
                return;
            }

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
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            connect.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void doPost(Request request, Response response) throws IOException {
        response.execute();
    }

    public void doGet(Request request, Response response) throws IOException {
        response.execute();
    }

}
