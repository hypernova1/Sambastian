package org.sam.server.context;

import java.lang.reflect.Method;

/**
 * 핸들러에 대한 정보를 저장하는 클래스입니다.
 *
 * @author hypernova1
 */
public class HandlerInfo {

    private final Object instance;

    private final Method handlerMethod;

    public HandlerInfo(Object instance, Method handlerMethod) {
        this.instance = instance;
        this.handlerMethod = handlerMethod;
    }

    /**
     * 핸들러 인스턴스를 반환합니다.
     *
     * @return 핸들러 인스턴스
     * */
    public Object getInstance() {
        return instance;
    }

    /**
     * 핸들러 메서드를 반환합니다.
     *
     * @return 핸들러 메서드
     * */
    public Method getMethod() {
        return handlerMethod;
    }
}
