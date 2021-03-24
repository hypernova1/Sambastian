package org.sam.server.http.context;

import org.sam.server.common.ServerProperties;

import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;

public class ServerSocketFactory {

    /**
     * 서버 소켓을 생성합니다.
     *
     * @author hypernova1
     * @return 서버 소켓
     * @throws IOException SSL 소켓 생성시에 네트워크 오류가 발생시
     * @see java.net.ServerSocket
     * @see javax.net.ssl.SSLServerSocket
     * */
    protected static ServerSocket createServerSocket() throws IOException {
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
    protected static ServerSocket createSSLServerSocket(String keyStore, String password, int port) throws IOException {
        ServerProperties.setSSL();
        System.setProperty("javax.net.ssl.keyStore", keyStore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
        System.setProperty("javax.net.debug", "ssl");
        SSLServerSocketFactory sslserversocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

        return sslserversocketfactory.createServerSocket(port);
    }

}
