package org.sam.server.bean;

import java.lang.reflect.Method;

/**
 * 핸들러에 대한 정보를 저장하는 클래스
 *
 * @author hypernova1
 */
public class Handler {

    private Object handleInstance;

    private Method handlerMethod;

    private Handler() {}

    public static Handler of(Object instance, Method handlerMethod) {
        Handler handler = new Handler();
        handler.handleInstance = instance;
        handler.handlerMethod = handlerMethod;
        return handler;
    }

    /**
     * 핸들러 인스턴스를 반환한다.
     *
     * @return 핸들러 인스턴스
     * */
    public Object getHandleInstance() {
        return handleInstance;
    }

    /**
     * 핸들러 메서드를 반환한다.
     *
     * @return 핸들러 메서드
     * */
    public Method getMethod() {
        return handlerMethod;
    }
}
