package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.context.BeanContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
public class HttpServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Socket connect;

    private HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void start() {
        try {
            ServerSocket serverSocket = createServerSocket();
            logger.info("server started..");
            logger.info("server port: " + serverSocket.getLocalPort());
            BeanContainer.createBeans();
            SessionManager.enableSessionChecker();

            ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
                    5,
                    200,
                    150L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>()
            );
            while (!Thread.currentThread().isInterrupted()) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());
                threadPool.execute(httpServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ServerSocket createServerSocket() throws IOException {
        String keyStore = ServerProperties.get("key-store");
        String keyStorePassword = ServerProperties.get("key-store.password");
        String propertiesPort = ServerProperties.get("server.port");
        int port = (propertiesPort != null) ? Integer.parseInt(propertiesPort) : 8080;
        if (System.getenv("PORT") != null) port = Integer.parseInt(System.getenv("PORT"));
        return keyStore != null ? createSSLServerSocket(keyStore, keyStorePassword, port) : new ServerSocket(port);
    }

    private static ServerSocket createSSLServerSocket(String keyStore, String password, int port) throws IOException {
        ServerProperties.setSSL();
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
        System.setProperty("javax.net.debug", "ssl");
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        return sslserversocketfactory.createServerSocket(port);
    }

    @Override
    public void run() {
        HttpLauncher.execute(connect);
        try {
            connect.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        static void enableSessionChecker() {
            new Timer().schedule(new SessionManager(), 0, 60 * 1000);
        }

        @Override
        public void run() {
            Iterator<Session> iterator = sessionList.iterator();
            while (iterator.hasNext()) {
                Session session = iterator.next();
                long accessTime = session.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long now = System.currentTimeMillis();
                int timeout = session.getTimeout() * 1000 * 1800;
                if (now - accessTime > timeout) {
                    iterator.remove();
                    logger.info("remove Session:" + session.getId());
                }
            }
        }
    }
}
