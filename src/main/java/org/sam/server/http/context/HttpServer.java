package org.sam.server.http.context;

import org.sam.server.ThreadPoolManager;
import org.sam.server.common.ServerProperties;
import org.sam.server.bean.BeanClassLoader;
import org.sam.server.bean.BeanLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * HTTP 서버의 시작점으로써, 서버 소켓을 생성하고 쓰헤드 풀을 생성하여 요청을 HttpLauncher로 위임한다.
 *
 * @author hypernova1
 * @see BeanLoader
 * @see HttpRequestDispatcherLauncher
 */
public class HttpServer {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    public static void start() throws ClassNotFoundException {
        try {
            ServerSocket serverSocket = ServerSocketFactory.createServerSocket();
            loadBeanContainer();
            logger.info("server started..");
            logger.info("server port: {}", serverSocket.getLocalPort());

            Class.forName("org.sam.server.http.SessionManager");

            ThreadPoolExecutor threadPoolExecutor = ThreadPoolManager.getThreadPoolExecutor();
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                HttpRequestHandler requestHandler = new HttpRequestHandler(clientSocket);
                threadPoolExecutor.execute(requestHandler);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadBeanContainer() {
        String rootPackageName = ServerProperties.get("root-package-name");
        new BeanLoader(new BeanClassLoader(rootPackageName)).load();
    }
}
