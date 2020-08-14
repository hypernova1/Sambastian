package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.context.BeanContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
@SuppressWarnings("unused")
public class HttpServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Socket connect;

    private HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void start() {
        String keyStore = ServerProperties.get("keyStore");
        String password = ServerProperties.get("keyStorePassword");
        String propertiesPort = ServerProperties.get("server.port");

        int port = 8080;
        if (propertiesPort != null) port = Integer.parseInt(propertiesPort);
        if (System.getenv("PORT") != null) port = Integer.parseInt(System.getenv("PORT"));
        try {
            ServerSocket serverSocket;
            if (keyStore != null) {
                ServerProperties.setSSL();
                System.setProperty("javax.net.ssl.keyStore", keyStore);
                System.setProperty("javax.net.ssl.keyStorePassword", password);
                System.setProperty("javax.net.debug", "ssl");
                SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
                serverSocket = sslserversocketfactory.createServerSocket(port);
            } else
                serverSocket = new ServerSocket(port);

            logger.info("server started..");
            logger.info("server port: " + port);

            BeanContainer.createBeans();
            new Timer().schedule(new SessionManager(), 0, 60 * 1000);

            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    5,
                    200,
                    150L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );
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
        HttpLauncher.execute(connect);
    }

    static class SessionManager extends TimerTask {

        private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
        private static final Set<Session> sessionList = new HashSet<>();

        private SessionManager() {}

        static void addSession(Session session) {
            sessionList.add(session);
        }

        static Session getSession(String id) {
            for (Session session : sessionList) {
                if (session.getId().equals(id)) {
                    return session;
                }
            }
            return null;
        }

        static void removeSession(String id) {
            sessionList.removeIf(session -> session.getId().equals(id));
        }

        @Override
        public void run() {
            Iterator<Session> iterator = sessionList.iterator();
            while (iterator.hasNext()) {
                Session session = iterator.next();
                long accessTime = session.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long now = System.currentTimeMillis();
                int timeout = session.getTimeout() * 1000;
                if (now - accessTime > timeout) {
                    iterator.remove();
                    logger.info("remove Session:" + session.getId());
                }
            }
        }
    }
}
