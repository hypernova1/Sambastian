package org.sam.server.context;

import java.lang.reflect.Method;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 10:37 AM
 */
public class HandlerInfo {

    private final Object instance;
    private final Method handlerMethod;

    public HandlerInfo(Object instance, Method handlerMethod) {
        this.instance = instance;
        this.handlerMethod = handlerMethod;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getMethod() {
        return handlerMethod;
    }
}
