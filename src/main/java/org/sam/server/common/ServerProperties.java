package org.sam.server.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 2:01 PM
 */
public class ServerProperties {

    private static final Logger logger = LoggerFactory.getLogger(ServerProperties.class);

    private static Properties properties = new Properties();
    public static boolean isSSL;

    static {
        loadClass();
    }

    public static void loadClass() {
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

    public static String get(String propertyKey) {
        return properties.getProperty(propertyKey);
    }

    public static void setSSL() {
        isSSL = true;
    }

    public static boolean isSSL() {
        return isSSL;
    }
}
