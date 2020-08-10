package org.sam.server;

import org.apache.log4j.Logger;
import org.sam.server.common.ServerProperties;
import org.sam.server.core.BeanContainer;
import org.sam.server.core.RequestReceiver;
import org.sam.server.http.SessionManager;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Timer;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpServer implements Runnable {
    private static final Logger logger = Logger.getLogger(HttpServer.class);
    public static final SessionManager sessionManager = new SessionManager();

    private final Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void start() {
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

            logger.info("server started..");
            logger.info("server port: " + port);

            BeanContainer.createBeans();

            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    5,
                    200,
                    150L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );

            new Timer().schedule(sessionManager, 0, 60 * 1000);
            while (!Thread.currentThread().isInterrupted()) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());
                logger.info("connected.." + LocalDateTime.now());
                logger.info("total thread count: " + threadPool.getPoolSize());
                threadPool.execute(httpServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        new RequestReceiver(connect).analyzeRequest();
    }
}
