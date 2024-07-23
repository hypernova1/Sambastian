package org.sam.server.common;

import org.sam.server.bean.ResourcesNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 서버의 설정을 관리하는 클래스
 *
 * @author hypernova1
 */
public class ServerProperties {

    private static final Logger logger = LoggerFactory.getLogger(ServerProperties.class);
    private static final Properties properties = new Properties();
    private static boolean isSSL;

    static {
        String resourceFileName = "application.properties";

        try (InputStream resourceAsStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("config/" + resourceFileName);) {

            if (resourceAsStream == null) {
                throw new ResourcesNotFoundException(resourceFileName);
            }

            properties.load(resourceAsStream);
        } catch (IOException e) {
            logger.error("properties loading error", e);
        }
    }

    /**
     * 서버 설정을 가져온다.
     *
     * @param key 설정 이름
     * @return 설정 값
     * */
    public static String get(String key) {
        return properties.getProperty(key);
    }

    /**
     * SSL 설정을 활성화 한다.
     * */
    public static void setSSL() {
        isSSL = true;
    }

    /**
     * SSL 설정이 되어 있는지 확인한다.
     *
     * @return SSL 설정 여부
     * */
    public static boolean isSSL() {
        return isSSL;
    }
}
