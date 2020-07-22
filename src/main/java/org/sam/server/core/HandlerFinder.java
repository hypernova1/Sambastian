package org.sam.server.core;

import org.sam.server.annotation.handle.*;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.HttpRequest;
import org.sam.server.http.HttpResponse;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 2:13 AM
 */
public class HandlerFinder {

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    private List<Class<? extends Annotation>> handleAnnotations =
            Arrays.asList(GetHandle.class, PostHandle.class, PutHandle.class, DeleteHandle.class);

    public HandlerFinder(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public HandlerInfo findHandlerMethod() throws NotFoundHandlerException {
        List<Class<?>> handlerClasses = BeanLoader.getHandlerClasses();
        for (Class<?> handlerClass : handlerClasses) {
            String requestPath = replaceRequestPath(handlerClass);
            Method handlerMethod = findMethod(handlerClass, requestPath);
            return new HandlerInfo(handlerClass, handlerMethod);
        }
        throw new NotFoundHandlerException();
    }

    private Method findMethod(Class<?> handlerClass, String requestPath) throws NotFoundHandlerException {
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Annotation[] declaredAnnotations = declaredMethod.getDeclaredAnnotations();
            for (Annotation declaredAnnotation : declaredAnnotations) {
                for (Class<? extends Annotation> handleAnnotation : handleAnnotations) {
                    if (handleAnnotation.equals(declaredAnnotation.annotationType())) {
                        try {
                            Method pathValue = handleAnnotation.getDeclaredMethod("value");
                            Method methodValue = handleAnnotation.getDeclaredMethod("method");
                            String path = pathValue.invoke(declaredAnnotation).toString();
                            String method = methodValue.invoke(declaredAnnotation).toString();

                            if (requestPath.equals(path) && httpRequest.getMethod().equals(HttpMethod.get(method))) {
                                if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null) {
                                    this.httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
                                }
                                return declaredMethod;
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        throw new NotFoundHandlerException();
    }

    private String replaceRequestPath(Class<?> handlerClass) {
        String requestPath = httpRequest.getPath();
        String handlerPath = handlerClass.getDeclaredAnnotation(Handler.class).value();
        if (!handlerPath.startsWith("/")) handlerPath = "/" + handlerPath;
        if (requestPath.startsWith(handlerPath)) {
            requestPath = replaceRequestPath(requestPath, handlerPath);
        }
        return requestPath;
    }

    private String replaceRequestPath(String path, String handlerPath) {
        int index = path.indexOf(handlerPath);
        path = path.substring(index + handlerPath.length());
        if (!path.startsWith("/")) path = "/" + path;
        return path;
    }
}
