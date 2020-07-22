package org.sam.server.constant;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:38 PM
 */
public enum ContentType {
    TEXT_PLAIN("text/plain", EncodingType.UTF_8),
    TEXT_HTML("text/html", EncodingType.UTF_8),
    APPLICATION_JSON("application/json", EncodingType.UTF_8),
    MULTIPART_FORM_DATA("multipart/form-data", EncodingType.UTF_8);

    private String value;
    private EncodingType encodingType;

    ContentType(String value, EncodingType encodingType) {
        this.value = value;
        this.encodingType = encodingType;
    }

    public void setEncodingType(EncodingType encodingType) {
        this.encodingType = encodingType;
    }

    public String getValue() {
        return this.value;
    }

    public EncodingType getEncodingType() {
        return this.encodingType;
    }
}
