package org.sam.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpServer implements Runnable {

    private Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(8080);
            System.out.println("server started..");

            while (true) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());
                System.out.println("connected..");
                Thread thread = new Thread(httpServer);
                thread.start();
                System.out.println("Thread Count: " + Thread.activeCount());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String fileRequested = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            String input = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);

            String method = parse.nextToken().toUpperCase();
            fileRequested = parse.nextToken().toLowerCase();

            out.println("HTTP/1.1 200 OK");
            out.println("Server: Java HTTP Server from sam : 1.0");
            out.println("Date: " + new Date());
            out.flush();
            dataOut.flush();
            connect.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
