package org.sam.server;

import org.sam.server.common.ServerProperties;
import org.sam.server.core.RequestReceiver;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpServer implements Runnable {

    public static boolean verbose = true;

    public static Class<?> applicationClass;

    private final Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void start() {

        ServerProperties.loadClass();
        String keyStore = ServerProperties.get("keyStore");
        String password = ServerProperties.get("keyStorePassword");
        int port = Integer.parseInt(ServerProperties.get("server.port"));

        try {
            ServerSocket serverSocket;

            if (keyStore != null) {
                ServerProperties.IS_SSL = true;

                System.setProperty("javax.net.ssl.keyStore", keyStore);
                System.setProperty("javax.net.ssl.keyStorePassword", password);
                System.setProperty("javax.net.debug", "ssl");

                SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                serverSocket = sslserversocketfactory.createServerSocket(port);
            } else {
                serverSocket = new ServerSocket(port);
            }

            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    5,
                    200,
                    150L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );

            if (verbose) {
                System.out.println("server started..");
                System.out.println("server port: " + port);

            }

            while (!Thread.currentThread().isInterrupted()) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());
                if (verbose) {
                    System.out.println("connected.." + LocalDateTime.now());
                }
                threadPool.execute(httpServer);
                System.out.println("total thread count: " + threadPool.getPoolSize());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        new RequestReceiver(connect).analyzeRequest();
    }
}
