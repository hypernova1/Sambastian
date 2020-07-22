package org.sam.server.core;

import org.sam.server.annotation.handle.*;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.Request;
import org.sam.server.http.Response;

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

    private final Request request;
    private final Response response;

    private List<Class<? extends Annotation>> handleAnnotations =
            Arrays.asList(GetHandle.class, PostHandle.class, PutHandle.class, DeleteHandle.class);

    public HandlerFinder(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    public HandlerInfo findHandlerMethod() throws NotFoundHandlerException {
        List<Class<?>> handlerClasses = BeanLoader.getHandlerClasses();
        for (Class<?> handlerClass : handlerClasses) {
            String requestPath = request.getPath();
            String handlerPath = handlerClass.getDeclaredAnnotation(Handler.class).value();
            if (!handlerPath.startsWith("/")) handlerPath = "/" + handlerPath;

            if (requestPath.startsWith(handlerPath)) {
                int index = requestPath.indexOf(handlerPath);
                requestPath = requestPath.substring(index + handlerPath.length());
                if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
            }

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
                        Method pathValue;
                        Method methodValue;
                        try {
                            pathValue = handleAnnotation.getDeclaredMethod("value");
                            methodValue = handleAnnotation.getDeclaredMethod("method");
                            String path = pathValue.invoke(declaredAnnotation).toString();
                            String method = methodValue.invoke(declaredAnnotation).toString();

                            if (requestPath.equals(path) && request.getMethod().equals(HttpMethod.get(method))) {
                                if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null) {
                                    this.response.setContentMimeType(ContentType.JSON);
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
}
