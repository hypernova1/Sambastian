package org.sam.server.constant;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:38 PM
 */
public enum ContentType {
    TEXT_PLAIN("text/plain"),
    TEXT_HTML("text/html"),
    APPLICATION_JSON("application/json"),
    CSS("text/css"),
    JAVASCRIPT("application/javascript"),
    X_ICON("image/x-icon"),
    PNG("image/png"),
    JPG("image/jpg"),
    JPEG("image/jpeg"),
    MULTIPART_FORM_DATA("multipart/form-data");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}
