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
 * HTTP 서버의 시작점으로써, 서버 소켓을 생성하고 쓰헤드 풀을 생성하여 요청을 HttpLauncher로 위임합니다.
 * 서버가 준비 되기 전에 Bean을 관리하는 BeanContainer와 세션을 관리하는 SessionManager를 초기화 합니다.
 *
 * @author hypernova1
 * @see org.sam.server.context.BeanContainer
 * @see org.sam.server.http.HttpLauncher
 * @see org.sam.server.http.HttpServer.SessionManager
 */
public class HttpServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Socket connect;

    private HttpServer(Socket connect) {
        this.connect = connect;
    }

    /**
     * 애플리케이션을 시작합니다. 서버가 종료될 때 까지 무한 루프를 돌며 요청을 HttpLauncher에 위임합니다.
     *
     * @author hypernova1
     * @see org.sam.server.http.HttpLauncher
     * */
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

    /**
     * 서버 소켓을 생성합니다.
     *
     * @author hypernova1
     * @return 서버 소켓
     * @throws IOException SSL 소켓 생성시에 네트워크 오류가 발생시
     * @see java.net.ServerSocket
     * @see javax.net.ssl.SSLServerSocket
     * */
    private static ServerSocket createServerSocket() throws IOException {
        String keyStore = ServerProperties.get("key-store");
        String keyStorePassword = ServerProperties.get("key-store.password");
        String propertiesPort = ServerProperties.get("server.port");
        int port = (propertiesPort != null) ? Integer.parseInt(propertiesPort) : 8080;
        if (System.getenv("PORT") != null) port = Integer.parseInt(System.getenv("PORT"));
        return keyStore != null ? createSSLServerSocket(keyStore, keyStorePassword, port) : new ServerSocket(port);
    }

    /**
     * SSL 서버 소켓을 생성합니다.
     *
     * @param keyStore keyStore 이름
     * @param password keyStore 비밀번호
     * @param port 포트 번호
     * @return SSL 서버 소켓
     * @throws IOException SSL 소켓 생성시에 네트워크 오류가 발생시
     * @see javax.net.ssl.SSLServerSocket
     * */
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

    /**
     * 세션을 관리하는 클래스입니다. 세션을 생성 및 삭제합니다. TimerTask를 상속 받아서 30분마다 유효한지 확인합니다.
     *
     * @see org.sam.server.http.Session
     * */
    static class SessionManager extends TimerTask {

        private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);
        private static final Set<Session> sessionList = new HashSet<>();

        private SessionManager() {}

        /**
         * 세션을 추가합니다.
         *
         * @param session 추가할 세션
         * */
        static void addSession(Session session) {
            sessionList.add(session);
        }

        /**
         * 세션을 가져옵니다.
         *
         * @param id 가져올 세션의 아이디
         * @return 세션
         * */
        static Session getSession(String id) {
            for (Session session : sessionList) {
                if (session.getId().equals(id)) {
                    return session;
                }
            }
            return null;
        }

        /**
         * 세션을 삭제합니다.
         *
         * @param id 삭제할 세션의 아이디
         * */
        static void removeSession(String id) {
            sessionList.removeIf(session -> session.getId().equals(id));
        }

        /**
         * 세션의 유효성을 확인합니다. 30분마다 현재 시간과 만료 시간을 비교하여 판단합니다.
         * */
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
