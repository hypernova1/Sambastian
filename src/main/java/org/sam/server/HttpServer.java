package org.sam.server;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Properties;

public class HttpServer implements Runnable {

    public static boolean verbose = true;

    private final Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void main(String[] args) throws IOException {

        Properties properties = getProperties();
        String keyStore = properties.getProperty("keyStore");
        String password = properties.getProperty("keyStorePassword");
        int port = Integer.parseInt(properties.getProperty("server.port"));

        try {

            ServerSocket serverSocket = null;

            if (keyStore != null) {
                System.setProperty("javax.net.ssl.keyStore", keyStore);
                System.setProperty("javax.net.ssl.keyStorePassword", password);
                System.setProperty("javax.net.debug", "ssl");

                SSLServerSocketFactory sslserversocketfactory =
                        (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                serverSocket =
                        sslserversocketfactory.createServerSocket(port);
            } else {
                serverSocket = new ServerSocket(port);
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

    private static Properties getProperties() throws IOException {
        Properties properties = new Properties();
        InputStream resourceAsStream = HttpServer.class.getClassLoader().getResourceAsStream("config/application.properties");

        if (resourceAsStream != null) {
            properties.load(resourceAsStream);
        }
        return properties;
    }

    public void run() {
        new Handler(connect).requestAnalyze();
    }
}
