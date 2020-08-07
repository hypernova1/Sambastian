package org.sam.server.core;

import org.sam.server.annotation.handle.*;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.exception.HandlerNotFoundException;
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

    private List<Class<? extends Annotation>> handlerAnnotationTypes =
            Arrays.asList(GetHandle.class, PostHandle.class, PutHandle.class, DeleteHandle.class);

    public HandlerFinder(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public HandlerInfo createHandlerInfo() throws HandlerNotFoundException {
        List<Class<?>> handlerClasses = BeanLoader.getHandlerClasses();
        for (Class<?> handlerClass : handlerClasses) {
            Method handlerMethod = findHandlerMethod(handlerClass);
            return new HandlerInfo(handlerClass, handlerMethod);
        }
        throw new HandlerNotFoundException();
    }

    private Method findHandlerMethod(Class<?> handlerClass) throws HandlerNotFoundException {
        String requestPath = replaceRequestPath(handlerClass);
        Method[] handlerClassDeclaredMethods = handlerClass.getDeclaredMethods();
        for (Method handlerClassDeclaredMethod : handlerClassDeclaredMethods) {
            Annotation[] handlerClassDeclaredMethodDeclaredAnnotations = handlerClassDeclaredMethod.getDeclaredAnnotations();
            for (Annotation handlerClassDeclaredMethodDeclaredAnnotation : handlerClassDeclaredMethodDeclaredAnnotations) {
                for (Class<? extends Annotation> handlerAnnotationType : handlerAnnotationTypes) {
                    boolean isSame = compareAnnotation(
                            requestPath, handlerClassDeclaredMethod,
                            handlerClassDeclaredMethodDeclaredAnnotation, handlerAnnotationType
                    );
                    if (isSame) return handlerClassDeclaredMethod;
                }
            }
        }
        throw new HandlerNotFoundException();
    }

    private boolean compareAnnotation(String requestPath,
                                      Method handlerClassDeclaredMethod,
                                      Annotation handlerClassDeclaredMethodDeclaredAnnotation,
                                      Class<? extends Annotation> handlerAnnotationType) {
        if (handlerAnnotationType.equals(handlerClassDeclaredMethodDeclaredAnnotation.annotationType())) {
            try {
                Method methodPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("method");
                Method pathPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("value");
                String path = pathPropertyInAnnotation.invoke(handlerClassDeclaredMethodDeclaredAnnotation).toString();
                String method = methodPropertyInAnnotation.invoke(handlerClassDeclaredMethodDeclaredAnnotation).toString();

                boolean isSame = compareMethodAndPath(requestPath, handlerClassDeclaredMethod, path, method);
                if (isSame) return true;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    private boolean compareMethodAndPath(String requestPath, Method declaredMethod, String path, String method) {
        if (requestPath.equals(path) && httpRequest.getMethod().equals(HttpMethod.get(method))) {
            if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null) {
                this.httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
            }
            return true;
        }
        return false;
    }

    private String replaceRequestPath(Class<?> handlerClass) {
        String requestPath = httpRequest.getPath();
        String pathValueInHandlerClass = handlerClass.getDeclaredAnnotation(Handler.class).value();
        if (!pathValueInHandlerClass.startsWith("/")) pathValueInHandlerClass = "/" + pathValueInHandlerClass;
        if (requestPath.startsWith(pathValueInHandlerClass)) {
            requestPath = replaceRequestPath(requestPath, pathValueInHandlerClass);
        }
        return requestPath;
    }

    private String replaceRequestPath(String requestPath, String handlerPath) {
        int index = requestPath.indexOf(handlerPath);
        requestPath = requestPath.substring(index + handlerPath.length());
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        return requestPath;
    }
}
