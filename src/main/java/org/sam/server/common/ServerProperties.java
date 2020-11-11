package org.sam.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 서버의 설정을 관리하는 클래스입니다.
 *
 * @author hypernova1
 */
public class ServerProperties {

    private static final Logger logger = LoggerFactory.getLogger(ServerProperties.class);

    private static final Properties properties = new Properties();
    private static boolean isSSL;

    static {
        load();
    }

    /**
     * 서버 설정을 담은 프로퍼티 파일을 읽습니다.
     * */
    private static void load() {
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("config/application.properties");
        if (resourceAsStream != null) {
            try {
                properties.load(resourceAsStream);
            } catch (IOException e) {
                logger.error("properties loading error", e);
            }
        }
    }

    /**
     * 서버 설정을 가져옵니다.
     *
     * @param key 설정 이름
     * @return 설정 값
     * */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * SSL 설정을 활성화 합니다.
     * */
    public static void setSSL() {
        isSSL = true;
    }

    /**
     * SSL 설정이 되어 있는지 확인합니다.
     * */
    public static boolean isSSL() {
        return isSSL;
    }
}
