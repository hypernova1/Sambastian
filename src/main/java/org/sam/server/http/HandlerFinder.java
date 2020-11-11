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
 * 실행할 핸들러를 찾는 클래스입니다.
 *
 * @author hypernova1
 */
public class HandlerFinder {

    private final HttpRequest httpRequest;

    private final HttpResponse httpResponse;

    private boolean isExistPath;

    private final List<Method> pathValueHandlerMethods = new ArrayList<>();

    private final List<Method> handlerMethods = new ArrayList<>();

    private HandlerFinder(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
    }

    /**
     * 인스턴스를 생성합니다.
     * */
    public static HandlerFinder of(HttpRequest httpRequest, HttpResponse httpResponse) {
        return new HandlerFinder(httpRequest, httpResponse);
    }

    /**
     * 핸들러 클래스를 탐색하여 해당하는 핸들러의 정보를 담은 인스턴스를 생성합니다.
     *
     * @return 핸들러 정보 인스턴스
     * @throws HandlerNotFoundException 홴들러를 찾지 못 했을 시
     * @see org.sam.server.context.HandlerInfo
     * */
    public HandlerInfo createHandlerInfo() throws HandlerNotFoundException {
        List<Object> handlerInstances = BeanContainer.getHandlerBeans();
        for (Object handlerInstance : handlerInstances) {
            Class<?> handlerClass = handlerInstance.getClass();
            classifyHandlers(handlerClass);
            Method handlerMethod = findHandlerMethod(handlerClass);
            if (handlerMethod != null)
                return new HandlerInfo(handlerInstance, handlerMethod);
        }
        if (this.httpRequest.getPath().equals("/") && this.httpRequest.getMethod().equals(HttpMethod.GET)) {
            httpResponse.responseIndexFile();
            return null;
        }
        if (httpRequest.getMethod().equals(HttpMethod.OPTIONS)) {
            httpResponse.executeOptionsResponse();
            return null;
        }
        if (isExistPath) {
            httpResponse.methodNotAllowed();
        }
        throw new HandlerNotFoundException();
    }

    /**
     * 핸들러 클래스 내부의 핸들러 메서드를 찾습니다.
     *
     * @param handlerClass 핸들러 클래스의 정보
     * @return 핸들러 메서드
     * @throws HandlerNotFoundException 핸들러를 찾지 못 했을 시
     * */
    private Method findHandlerMethod(Class<?> handlerClass) throws HandlerNotFoundException {

        String requestPath = replaceRequestPath(handlerClass);
        String handlerClassPath = handlerClass.getDeclaredAnnotation(Handler.class).value();
        Method handlerClassDeclaredMethod = findHandlerMethod(requestPath, handlerClassPath, handlerMethods);
        if (handlerClassDeclaredMethod == null) {
            handlerClassDeclaredMethod = findHandlerMethod(requestPath, handlerClassPath, pathValueHandlerMethods);
        }
        return handlerClassDeclaredMethod;
    }

    /**
     * 어노테이션의 정보와 요청 정보가 일치하는 핸들러의 메서드를 반환합니다.
     *
     * @param requestPath 요청 URL
     * @param handlerClassPath 핸들러 클래스의 URL
     * @param handlerMethods 핸들러 메서드 목록
     * @return 핸들러 메서드
     * */
    private Method findHandlerMethod(String requestPath, String handlerClassPath, List<Method> handlerMethods) {
        for (Method handlerMethod : handlerMethods) {
            Annotation[] handlerMethodDeclaredAnnotations = handlerMethod.getDeclaredAnnotations();
            for (Annotation handlerMethodDeclaredAnnotation : handlerMethodDeclaredAnnotations) {
                if (handlerMethodDeclaredAnnotation.annotationType().getDeclaredAnnotation(Handle.class) != null) {
                    boolean compareAnnotation = compareAnnotation(requestPath, handlerClassPath, handlerMethod, handlerMethodDeclaredAnnotation);
                    if (compareAnnotation) return handlerMethod;
                }
            }
        }
        return null;
    }

    /**
     * 일반 핸들러 메서드와 url 내에 파라미터가 있는 핸들러를 분류합니다.
     *
     * @param handlerClass 핸들러 클래스
     * */
    private void classifyHandlers(Class<?> handlerClass) {
        Method[] handlerClassDeclaredMethods = handlerClass.getDeclaredMethods();
        Arrays.stream(handlerClassDeclaredMethods).forEach(handlerMethod -> {
            for (Annotation annotation : handlerMethod.getDeclaredAnnotations()) {
                Handle handle = annotation.annotationType().getDeclaredAnnotation(Handle.class);
                if (handle != null) {
                    classifyHandlers(handlerMethod, annotation);
                }
            }
        });
    }

    /**
     * 일반 핸들러 메서드와 url 내에 파라미터가 있는 핸들러를 분류합니다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param annotation 핸들러 메서드의 어노테이션
     * */
    private void classifyHandlers(Method handlerMethod, Annotation annotation) {
        try {
            Method value = annotation.annotationType().getMethod("value");
            String path = String.valueOf(value.invoke(annotation));
            if (path.contains("{")) {
                pathValueHandlerMethods.add(handlerMethod);
            } else {
                handlerMethods.add(handlerMethod);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 요청 정보와 핸들러 메서드의 정보가 일치하는지 비교합니다.
     *
     * @param requestPath 요청 URL
     * @param handlerClassPath 핸들러 클래스의 URL
     * @param handlerMethod 핸들러 메서드
     * @param handlerMethodDeclaredAnnotation 핸들러 메서드에 선언된 어노테이션
     * @return 일치 여부
     * */
    private boolean compareAnnotation(String requestPath, String handlerClassPath, Method handlerMethod, Annotation handlerMethodDeclaredAnnotation) {
        Class<? extends Annotation> handlerAnnotationType = handlerMethodDeclaredAnnotation.annotationType();
        try {
            Method methodPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("method");
            Method pathPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("value");
            String path = pathPropertyInAnnotation.invoke(handlerMethodDeclaredAnnotation).toString();
            if (!path.startsWith("/")) path = "/" + path;
            if (requestPath.equals(httpRequest.getPath())) {
                path = handlerClassPath + path;
            }
            String method = methodPropertyInAnnotation.invoke(handlerMethodDeclaredAnnotation).toString();
            boolean isSame = compareMethodAndPath(requestPath, handlerMethod, path, method);
            if (isSame) return true;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * URL과 HTTP Method가 일치하는지 비교합니다.
     *
     * @param requestPath 요청 URL
     * @param handlerMethod 핸들러 메서드
     * @param path 핸들러 메서드의 URL
     * @param method 헨들러 메서드의 HTTP Method
     * @return 일치 여부
     * */
    private boolean compareMethodAndPath(String requestPath, Method handlerMethod, String path, String method) {
        HttpMethod httpMethod = httpRequest.getMethod();
        boolean containPathValue = findPathValueAnnotation(handlerMethod);
        boolean isSamePath = requestPath.equals(path);
        if (isSamePath) {
            this.isExistPath = true;
        }
        if (containPathValue) {
            isSamePath = findPathValueHandler(requestPath, path, isSamePath);
        }
        boolean isOptionsRequest = httpMethod.equals(HttpMethod.OPTIONS);
        if (isSamePath && isOptionsRequest) {
            httpResponse.addAllowedMethod(HttpMethod.get(method));
        }
        boolean isHeadRequest = httpMethod.equals(HttpMethod.HEAD) && HttpMethod.GET.toString().equals(method);
        if (!isOptionsRequest && isSamePath && httpMethod.equals(HttpMethod.get(method)) || isHeadRequest) {
            if (handlerMethod.getDeclaredAnnotation(RestApi.class) != null)
                this.httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
            return true;
        }
        return false;
    }

    /**
     * URL에 파라미터가 포함된 핸들러 메서드를 찾습니다.
     *
     * @param requestPath 요청 URL
     * @param path 핸들러 메서드의 URL
     * @param isSamePath 기존 일치 여부
     * @return 일치 여부
     * */
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

    /**
     * 핸들러 메서드에 @PathValue 어노테이션이 선언되어 있는지 확인합니다.
     *
     * @param handlerMethod 핸들러 메서드
     * @return @PathValue 선언 여부
     * */
    private boolean findPathValueAnnotation(Method handlerMethod) {
        Parameter[] parameters = handlerMethod.getParameters();
        for (Parameter parameter : parameters) {
            if (parameter.getDeclaredAnnotation(PathValue.class) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 경로에 URL이 포함 된 핸들러 메서드가 일치하는 지 확인합니다.
     *
     * @param requestPath 요청 URL
     * @param path 핸들러 메서드 URL
     * @param paramNames 핸들러 메서드 파라미터
     * @return 일치 여부
     * */
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

    /**
     * 요청 URL과 핸들러 클래스의 URL을 비교하고 처음 부분이 일치한다면 그 부분만큼 요청 URL을 잘라내고 반환합니다.
     *
     * @param 핸들러 클래스 정보
     * @return 수정된 요청 URL
     * */
    private String replaceRequestPath(Class<?> handlerClass) {
        String requestPath = httpRequest.getPath();
        String rootRequestPath = "/";
        if (!requestPath.equals("/")) {
            rootRequestPath += requestPath.split("/")[1];
        }
        String handlerClassPath = handlerClass.getDeclaredAnnotation(Handler.class).value();
        if (!handlerClassPath.startsWith("/"))
            handlerClassPath = "/" + handlerClassPath;
        if (rootRequestPath.equals(handlerClassPath))
            requestPath = replaceRequestPath(requestPath, handlerClassPath);
        if (!requestPath.equals("/") && requestPath.endsWith("/"))
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        return requestPath;
    }

    /**
     * 요청 URL과 핸들러 클래스의 URL을 비교하고 처음 부분이 일치한다면 그 부분만큼 요청 URL을 잘라내고 반환합니다.
     *
     * @param requestPath 요청 URL
     * @param handlerPath 핸들러 클래스 URL
     * @return 수정된 요청 URL
     * */
    private String replaceRequestPath(String requestPath, String handlerPath) {
        int index = requestPath.indexOf(handlerPath);
        requestPath = requestPath.substring(index + handlerPath.length());
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        return requestPath;
    }
}
