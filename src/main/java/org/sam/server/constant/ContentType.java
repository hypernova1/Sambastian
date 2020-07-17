package org.sam.server.constant;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:38 PM
 */
public enum ContentType {
    PLAIN("text/plain"),
    HTML("text/html"),
    JSON("application/json");

    private String value;
    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}
