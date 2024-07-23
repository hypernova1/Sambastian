package org.sam.server.bean;

/**
 * 요청에 대한 핸들러를 찾지 못했을 시 발생한다.
 *
 * @author hypernova1
 * */
public class HandlerNotFoundException extends RuntimeException {
    public HandlerNotFoundException() {
        super("handler not found");
    }
}
