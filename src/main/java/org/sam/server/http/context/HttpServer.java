package org.sam.server.http.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * HTTP 서버의 시작점으로써, 서버 소켓을 생성하고 쓰헤드 풀을 생성하여 요청을 HttpLauncher로 위임합니다.
 *
 * @author hypernova1
 * @see org.sam.server.context.BeanContainer
 * @see HttpLauncher
 */
public class HttpServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private final Socket connect;

    private HttpServer(Socket connect) {
        this.connect = connect;
    }

    /**
     * 애플리케이션을 시작합니다. 서버가 종료될 때 까지 무한 루프를 돌며 쓰레드를 생성하고 요청을 HttpLauncher에 위임합니다.
     *
     * @author hypernova1
     * @see HttpLauncher
     * */
    public static void start() {
        try {
            ServerSocket serverSocket = ServerSocketFactory.createServerSocket();
            logger.info("server started..");
            logger.info("server port: " + serverSocket.getLocalPort());

            ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                    Runtime.getRuntime().availableProcessors(),
                    200,
                    150L,
                    TimeUnit.SECONDS,
                    new LinkedBlockingDeque<>()
            );
            while (!Thread.currentThread().isInterrupted()) {
                Socket clientSocket = serverSocket.accept();
                HttpServer httpServer = new HttpServer(clientSocket);
                threadPoolExecutor.execute(httpServer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
