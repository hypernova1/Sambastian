package org.sam.server.http;

import org.sam.server.annotation.component.Handler;
import org.sam.server.annotation.handle.Handle;
import org.sam.server.annotation.handle.PathValue;
import org.sam.server.annotation.handle.RestApi;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 2:13 AM
 */
public class HandlerFinder {

    private final HttpRequest httpRequest;
    private final HttpResponse httpResponse;

    private HandlerFinder(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    public static HandlerFinder of(HttpRequest httpRequest, HttpResponse httpResponse) {
        return new HandlerFinder(httpRequest, httpResponse);
    }

    public HandlerInfo createHandlerInfo() throws HandlerNotFoundException {
        List<Object> handlerClasses = BeanContainer.getHandlerBeans();
        for (Object handlerClass : handlerClasses) {
            Method handlerMethod = findHandlerMethod(handlerClass.getClass());
            if (handlerMethod != null)
                return new HandlerInfo(handlerClass, handlerMethod);
        }

        if (this.httpRequest.getPath().equals("/") && this.httpRequest.getMethod().equals(HttpMethod.GET)) {
            httpResponse.returnIndexFile();
            return null;
        }
        throw new HandlerNotFoundException();
    }

    private Method findHandlerMethod(Class<?> handlerClass) throws HandlerNotFoundException {
        String requestPath = replaceRequestPath(handlerClass);
        String pathValueInHandlerClass = handlerClass.getDeclaredAnnotation(Handler.class).value();
        Method[] handlerClassDeclaredMethods = handlerClass.getDeclaredMethods();
//        Arrays.stream(handlerClassDeclaredMethods).filter(handlerMethod -> {
//        });
        for (Method handlerClassDeclaredMethod : handlerClassDeclaredMethods) {
            Annotation[] handlerClassDeclaredMethodDeclaredAnnotations = handlerClassDeclaredMethod.getDeclaredAnnotations();
            for (Annotation handlerClassDeclaredMethodDeclaredAnnotation : handlerClassDeclaredMethodDeclaredAnnotations) {
                if (handlerClassDeclaredMethodDeclaredAnnotation.annotationType().getDeclaredAnnotation(Handle.class) != null) {
                    boolean isSame = compareAnnotation(requestPath, pathValueInHandlerClass, handlerClassDeclaredMethod, handlerClassDeclaredMethodDeclaredAnnotation, handlerClassDeclaredMethodDeclaredAnnotation.annotationType());
                    if (isSame)
                        return handlerClassDeclaredMethod;
                }
            }
        }

        return null;
    }

    private boolean compareAnnotation(
            String requestPath, String pathValueInHandlerClass,
            Method handlerClassDeclaredMethod,
            Annotation handlerClassDeclaredMethodDeclaredAnnotation,
            Class<? extends Annotation> handlerAnnotationType
    ) {
        if (handlerAnnotationType.equals(handlerClassDeclaredMethodDeclaredAnnotation.annotationType())) {
            try {
                Method methodPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("method");
                Method pathPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("value");
                String path = pathPropertyInAnnotation.invoke(handlerClassDeclaredMethodDeclaredAnnotation).toString();
                if (!path.startsWith("/")) path = "/" + path;
                if (requestPath.equals(httpRequest.getPath())) {
                    path = pathValueInHandlerClass + path;
                }
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
        boolean containPathValue = findPathValueAnnotation(declaredMethod);;
        boolean isSamePath = requestPath.equals(path);
        if (containPathValue) {
            isSamePath = findPathValueHandler(requestPath, path, isSamePath);
        }
        if (isSamePath && httpRequest.getMethod().equals(HttpMethod.get(method))) {
            if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null)
                this.httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
            return true;
        }
        return false;
    }

    private boolean findPathValueHandler(String requestPath, String path, boolean isSamePath) {
        Pattern pattern = Pattern.compile("[{](.*?)[}]");
        Matcher matcher = pattern.matcher(path);
        Queue<String> paramNames = new ArrayDeque<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        if (!paramNames.isEmpty()) {
            isSamePath = matchPath(requestPath, path, paramNames);
        }
        return isSamePath;
    }

    private boolean findPathValueAnnotation(Method declaredMethod) {
        Parameter[] parameters = declaredMethod.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.getDeclaredAnnotation(PathValue.class) != null) {
                return true;
            }
        }
        return false;
    }

    private boolean matchPath(String requestPath, String path, Queue<String> paramNames) {
        if (!path.contains("{")) return false;
        String[] requestPathArr = requestPath.split("/");
        String[] pathArr = path.split("/");
        Map<String, String> param = new HashMap<>();
        if (requestPathArr.length != pathArr.length) return false;
        for (int i = 0; i < pathArr.length; i++) {
            if (pathArr[i].contains("{")) {
                param.put(paramNames.poll(), requestPathArr[i]);
                continue;
            }
            if (!pathArr[i].equals(requestPathArr[i])) {
                return false;
            }
        }
        httpRequest.getParameters().putAll(param);
        return true;
    }


    private String replaceRequestPath(Class<?> handlerClass) {
        String requestPath = httpRequest.getPath();
        String pathValueInHandlerClass = handlerClass.getDeclaredAnnotation(Handler.class).value();
        if (!pathValueInHandlerClass.startsWith("/"))
            pathValueInHandlerClass = "/" + pathValueInHandlerClass;
        if (requestPath.startsWith(pathValueInHandlerClass))
            requestPath = replaceRequestPath(requestPath, pathValueInHandlerClass);
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
