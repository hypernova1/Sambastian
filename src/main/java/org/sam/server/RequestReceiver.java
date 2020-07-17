package org.sam.server;

import org.sam.server.constant.HttpMethod;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public abstract class RequestReceiver {

    private final Socket connect;

    public RequestReceiver(Socket connect) {
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

            findHandler(request, response);

            connect.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findHandler(Request request, Response response) throws IOException {
        try {
            switch (request.getMethod()) {
                case GET: doGet(request, response); break;
                case POST: doPost(request, response); break;
                case PUT: doPut(request, response); break;
                case DELETE: doDelete(request, response); break;
                case HEAD: doHead(request, response); break;
                case OPTIONS: doOptions(request, response); break;
                case TRACE: doTrace(request, response); break;
                case CONNECT: doConnect(request, response); break;
                case PATCH: doPatch(request, response); break;
                default:
            }
            response.execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(Request request, Response response) throws IOException {}
    protected void doPost(Request request, Response response) throws IOException {}
    protected void doPut(Request request, Response response) throws IOException {}
    protected void doDelete(Request request, Response response) throws IOException {}
    protected void doHead(Request request, Response response) throws IOException {}
    protected void doOptions(Request request, Response response) throws IOException {}
    protected void doTrace(Request request, Response response) throws IOException {}
    protected void doConnect(Request request, Response response) throws IOException {}
    protected void doPatch(Request request, Response response) throws IOException {}


}
