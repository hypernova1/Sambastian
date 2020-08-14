package org.sam.server.http;

import org.sam.server.annotation.component.Handler;
import org.sam.server.annotation.handle.*;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.context.BeanClassLoader;
import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;

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

    protected HandlerFinder(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public HandlerInfo createHandlerInfo() throws HandlerNotFoundException {
        List<Class<?>> handlerClasses = BeanClassLoader.getHandlerClasses();
        for (Class<?> handlerClass : handlerClasses) {
            Method handlerMethod = findHandlerMethod(handlerClass);
            if (handlerMethod != null)
                return new HandlerInfo(handlerClass, handlerMethod);
        }
        if (this.httpRequest.getPath().equals("/")
                && this.httpRequest.getMethod().equals(HttpMethod.GET)) {
            httpResponse.returnIndexFile();
            return null;
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
                    boolean isSame = compareAnnotation(requestPath, handlerClassDeclaredMethod, handlerClassDeclaredMethodDeclaredAnnotation, handlerAnnotationType);
                    if (isSame)
                        return handlerClassDeclaredMethod;
                }
            }
        }
        return null;
    }

    private boolean compareAnnotation(
            String requestPath,
            Method handlerClassDeclaredMethod,
            Annotation handlerClassDeclaredMethodDeclaredAnnotation,
            Class<? extends Annotation> handlerAnnotationType
    ) {
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
            if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null)
                this.httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
            return true;
        }
        return false;
    }

    private String replaceRequestPath(Class<?> handlerClass) {
        String requestPath = httpRequest.getPath();
        String pathValueInHandlerClass = handlerClass.getDeclaredAnnotation(Handler.class).value();
        if (!pathValueInHandlerClass.startsWith("/"))
            pathValueInHandlerClass = "/" + pathValueInHandlerClass;
        if (requestPath.startsWith(pathValueInHandlerClass))
            requestPath = replaceRequestPath(requestPath, pathValueInHandlerClass);
        if (!pathValueInHandlerClass.equals("/") && requestPath.equals(httpRequest.getPath()))
            requestPath = pathValueInHandlerClass + requestPath;
        if (!requestPath.equals("/") && requestPath.endsWith("/"))
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        return requestPath;
    }

    private String replaceRequestPath(String requestPath, String handlerPath) {
        int index = requestPath.indexOf(handlerPath);
        requestPath = requestPath.substring(index + handlerPath.length());
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        return requestPath;
    }
}
