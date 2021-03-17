package org.sam.server.constant;

/**
 * 미디어 타입에 대한 상수입니다.
 *
 * @author hypernova1
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

    public static ContentType get(String name) {
        for(ContentType contentType : values()){
            if(contentType.value.equals(name)){
                return contentType;
            }
        }
        return TEXT_HTML;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
