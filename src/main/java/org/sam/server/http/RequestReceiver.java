package org.sam.server.http;

import org.sam.server.constant.HttpMethod;
import org.sam.server.core.BeanLoader;
import org.sam.server.http.Request;
import org.sam.server.http.Response;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

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

        List<Class<?>> handlerClasses = BeanLoader.getHandlerClasses();



        try {

            response.execute();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
