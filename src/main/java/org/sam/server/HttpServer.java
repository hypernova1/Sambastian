package org.sam.server;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public class HttpServer implements Runnable {

    private static final int PORT = 8080;
    public static boolean verbose = true;

    private final Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void main(String[] args) {

        try {

            ServerSocket serverSocket = null;

            if (args[0] != null) {
                System.setProperty("javax.net.ssl.keyStore", args[0]);
                System.setProperty("javax.net.ssl.keyStorePassword", args[1]);
                System.setProperty("javax.net.debug", "ssl");

                SSLServerSocketFactory sslserversocketfactory =
                        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                serverSocket =
                        sslserversocketfactory.createServerSocket(PORT);
            } else {
                serverSocket = new ServerSocket(PORT);
            }

            if (verbose) {
                System.out.println("server started..");
            }

            while (true) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());

                if (verbose) {
                    System.out.println("connected.." + new Date());
                }
                Thread thread = new Thread(httpServer);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        new Handler(connect).requestAnalyze();
    }
}
