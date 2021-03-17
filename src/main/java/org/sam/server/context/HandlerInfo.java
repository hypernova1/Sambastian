package org.sam.server.context;

import java.lang.reflect.Method;

/**
 * 핸들러에 대한 정보를 저장하는 클래스입니다.
 *
 * @author hypernova1
 */
public class HandlerInfo {

    private Object instance;

    private Method handlerMethod;

    private HandlerInfo() {}

    public static HandlerInfo of(Object instance, Method handlerMethod) {
        HandlerInfo handlerInfo = new HandlerInfo();
        handlerInfo.instance = instance;
        handlerInfo.handlerMethod = handlerMethod;
        return handlerInfo;
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
