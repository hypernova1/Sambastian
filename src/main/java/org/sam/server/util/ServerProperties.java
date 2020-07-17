package org.sam.server.util;

import org.sam.server.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 2:01 PM
 */
public class ServerProperties {

    private static Properties properties = new Properties();

    static {
        InputStream resourceAsStream = ServerProperties.class.getClassLoader().getResourceAsStream("config/application.properties");
        if (resourceAsStream != null) {
            try {
                properties.load(resourceAsStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String get(String propertyKey) {
        return properties.getProperty(propertyKey);
    }
}