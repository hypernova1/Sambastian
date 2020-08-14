package org.sam.server.context;

import java.lang.reflect.Method;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 10:37 AM
 */
public class HandlerInfo {

    private final Class<?> handlerClass;
    private final Method handlerMethod;

    public HandlerInfo(Class<?> handlerClass, Method handlerMethod) {
        this.handlerClass = handlerClass;
        this.handlerMethod = handlerMethod;
    }

    public Class<?> getHandlerClass() {
        return handlerClass;
    }

    public Method getHandlerMethod() {
        return handlerMethod;
    }
}
