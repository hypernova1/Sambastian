package org.sam.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpServer implements Runnable {




    private static final int PORT = 8080;


    private final Socket connect;

    private final Handler handler;

    public HttpServer(Socket connect) {
        this.connect = connect;
        this.handler = new Handler(connect);
    }

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("server started..");

            while (true) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());

                if (Handler.verbose) {
                    System.out.println("connected.." + new Date());
                }
                Thread thread = new Thread(httpServer);
                thread.start();
                System.out.println("Thread Count: " + Thread.activeCount());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            handler.analyze(in, out, dataOut);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }








}
