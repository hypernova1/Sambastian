package org.sam.server.http.context;

import org.sam.server.annotation.handle.PathValue;
import org.sam.server.annotation.handle.RequestMapping;
import org.sam.server.annotation.handle.RestApi;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.Handler;
import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * 실행할 핸들러를 찾는 클래스
 *
 * @author hypernova1
 */
public class HandlerFinder {

    private static final Pattern PATH_VALUE_PATTERN = Pattern.compile("[{](.*?)[}]");

    private final BeanContainer beanContainer = BeanContainer.getInstance();

    private final Request request;

    private final Response response;

    private final List<Method> pathValueHandlerMethods = new ArrayList<>();

    private final List<Method> handlerMethods = new ArrayList<>();

    private String handlerClassPath;

    private boolean existsPath;

    private HandlerFinder(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 인스턴스를 생성한다.
     *
     * @param request 요청 인스턴스
     * @param response 응답 인스턴스
     * @return HandlerFinder 인스턴스
     * */
    public static HandlerFinder of(Request request, Response response) {
        return new HandlerFinder(request, response);
    }

    /**
     * 핸들러 클래스를 탐색하여 해당하는 핸들러의 정보를 담은 인스턴스를 생성한다.
     *
     * @return 핸들러 정보 인스턴스
     * @throws HandlerNotFoundException 홴들러를 찾지 못 했을 시
     * @see Handler
     *
     * TODO: 핸들러 찾는 알고리즘 변경해야함.
     * */
    public Handler createHandlerInfo() throws HandlerNotFoundException {
        List<Object> handlerInstances = beanContainer.getHandlerBeans();
        for (Object handlerInstance : handlerInstances) {
            Class<?> handlerType = handlerInstance.getClass();
            classifyHandler(handlerType);
            this.handlerClassPath = handlerType.getDeclaredAnnotation(org.sam.server.annotation.component.Handler.class).value();
            Method handlerMethod = findHandlerMethod();
            if (handlerMethod == null) continue;
            if (!handlerInstance.getClass().equals(handlerMethod.getDeclaringClass())) {
                continue;
            }
            return Handler.of(handlerInstance, handlerMethod);
        }

//        if (existsPath) {
//            response.methodNotAllowed();
//        }
        throw new HandlerNotFoundException();
    }

    /**
     * 핸들러 클래스 내부의 핸들러 메서드를 찾는다.
     *
     * @return 핸들러 메서드
     * @throws HandlerNotFoundException 핸들러를 찾지 못 했을 시
     * */
    private Method findHandlerMethod() throws HandlerNotFoundException {
        return Stream.of(
                findHandlerMethod(handlerMethods),
                findHandlerMethod(pathValueHandlerMethods)
        )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElse(null);
    }

    /**
     * 어노테이션의 정보와 요청 정보가 일치하는 핸들러의 메서드를 반환한다.
     *
     * @param handlerMethods 핸들러 메서드 목록
     * @return 핸들러 메서드
     * */
    private Optional<Method> findHandlerMethod(List<Method> handlerMethods) {
        return handlerMethods.stream()
                .filter(this::isMatchHandlerMethod)
                .findFirst();
    }

    /**
     * 핸들러 메서드가 요청과 일치히는지 확인한다.
     *
     * @param handlerMethod 한들러 메서드
     * @return 일치여부
     * */
    private boolean isMatchHandlerMethod(Method handlerMethod) {
        Annotation[] annotationsDeclaredOnHandlerMethod = handlerMethod.getDeclaredAnnotations();
        return Arrays.stream(annotationsDeclaredOnHandlerMethod)
                .filter(this::isHandlerAnnotation)
                .anyMatch(annotation -> isMatchedHandlerMethod(handlerMethod, annotation));
    }

    /**
     * 일반 핸들러 메서드와 url 내에 파라미터가 있는 핸들러를 분류한다.
     *
     * @param handlerClass 핸들러 클래스
     * */
    private void classifyHandler(Class<?> handlerClass) {
        Method[] handlerMethods = handlerClass.getDeclaredMethods();
        for (Method handlerMethod : handlerMethods) {
            classifyHandlerMethod(handlerMethod);
        }
    }

    /**
     * 일반 핸들러 메서드와 url 내에 파라미터가 있는 핸들러를 분류한다.
     *
     * @param handlerMethod 핸들러 메서드
     * */
    private void classifyHandlerMethod(Method handlerMethod) {
        for (Annotation annotation : handlerMethod.getDeclaredAnnotations()) {
            if (!isHandlerAnnotation(annotation)) continue;
            classifyHandler(handlerMethod, annotation);
        }
    }

    /**
     * 일반 핸들러 메서드와 url 내에 파라미터가 있는 핸들러를 분류한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerAnnotation 핸들러 메서드의 어노테이션
     * */
    private void classifyHandler(Method handlerMethod, Annotation handlerAnnotation) {
        try {
            Method value = handlerAnnotation.annotationType().getMethod("value");
            String path = String.valueOf(value.invoke(handlerAnnotation));
            if (containsUrlParameter(path)) {
                pathValueHandlerMethods.add(handlerMethod);
                return;
            }
            handlerMethods.add(handlerMethod);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 요청 정보와 핸들러 메서드의 정보가 일치하는지 비교한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param annotation 핸들러 메서드에 선언된 어노테이션
     * @return 일치 여부
     * */
    private boolean isMatchedHandlerMethod(Method handlerMethod, Annotation annotation) {

        try {
            String requestUrl = getRequestUrlWithoutHandlerPath();
            String handlerMethodPath = getHandlerMethodPath(annotation);
            if (requestUrl.equals(request.getUrl())) {
                handlerMethodPath = this.handlerClassPath + handlerMethodPath;
            }


            HttpMethod handlerHttpMethod = getHandlerHttpMethod(annotation);
            if (isMatchedHandlerMethod(handlerMethod, handlerMethodPath, handlerHttpMethod)) {
                return true;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 핸들러 메서드의 url을 가져온다.
     *
     * @param annotation 핸들러 메서드의 어노테이션
     * @return url
     * */
    private String getHandlerMethodPath(Annotation annotation) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Method pathPropertyInAnnotation = annotation.annotationType().getDeclaredMethod("value");
        String handlerMethodPath = pathPropertyInAnnotation.invoke(annotation).toString();
        if (!handlerMethodPath.startsWith("/")) {
            handlerMethodPath = "/" + handlerMethodPath;
        }
        return handlerMethodPath;
    }

    /**
     * URL과 HTTP Method가 일치하는지 비교한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerMethodPath 핸들러 메서드의 URL
     * @param handlerHttpMethod 헨들러 메서드의 HTTP Method
     * @return 일치 여부
     * */
    private boolean isMatchedHandlerMethod(Method handlerMethod, String handlerMethodPath, HttpMethod handlerHttpMethod) {
        HttpMethod httpMethod = request.getMethod();

        if (isMatchedPath(handlerMethod, handlerMethodPath)) {
            if (httpMethod.equals(HttpMethod.OPTIONS)) {
                response.addAllowedMethod(handlerHttpMethod);
            }

            if (!httpMethod.equals(HttpMethod.OPTIONS) && httpMethod.equals(handlerHttpMethod) || isHeadRequest(handlerHttpMethod)) {
                if (handlerMethod.getDeclaredAnnotation(RestApi.class) != null) {
                    this.response.setContentMimeType(ContentType.APPLICATION_JSON);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * 핸들러 메서드의 url과 요청 url이 같은지에 대한 여부를 반환한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerMethodPath 핸들러 메서드의 url
     * */
    private boolean isMatchedPath(Method handlerMethod, String handlerMethodPath) {
        String requestPath = getRequestUrlWithoutHandlerPath();

        if (existsPathValueAnnotation(handlerMethod)) {
            return isMatchedPath(requestPath, handlerMethodPath);
        } else {
            if (requestPath.equals(handlerMethodPath)) {
                this.existsPath = true;
                return true;
            }
        }
        return false;
    }

    /**
     * URL에 파라미터가 포함된 핸들러 메서드를 찾는다.
     *
     * @param requestPath 요청 URL
     * @param path 핸들러 메서드의 URL
     * @return 일치 여부
     * */
    private boolean isMatchedPath(String requestPath, String path) {
        Matcher matcher = PATH_VALUE_PATTERN.matcher(path);
        Queue<String> paramNames = new ArrayDeque<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        if (!paramNames.isEmpty()) {
            return isMatchPath(requestPath, path, paramNames);
        }
        return false;
    }

    /**
     * 핸들러 메서드에 PathValue 어노테이션이 선언되어 있는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @return PathValue 선언 여부
     * */
    private boolean existsPathValueAnnotation(Method handlerMethod) {
        Parameter[] parameters = handlerMethod.getParameters();
        return Arrays.stream(parameters)
                .anyMatch(this::isDeclaredPathValueAnnotation);
    }

    /**
     * 경로에 URL이 포함 된 핸들러 메서드가 일치하는 지 확인한다.
     *
     * @param requestPath 요청 URL
     * @param path 핸들러 메서드 URL
     * @param paramNames 핸들러 메서드 파라미터
     * @return 일치 여부
     * */
    private boolean isMatchPath(String requestPath, String path, Queue<String> paramNames) {
        if (!containsUrlParameter(path)) return false;
        String[] requestPathArr = requestPath.split("/");
        String[] pathArr = path.split("/");
        Map<String, String> param = new HashMap<>();
        if (requestPathArr.length != pathArr.length) {
            return false;
        }
        for (int i = 0; i < pathArr.length; i++) {
            if (containsUrlParameter(pathArr[i])) {
                param.put(paramNames.poll(), requestPathArr[i]);
                continue;
            }
            if (!pathArr[i].equals(requestPathArr[i])) {
                return false;
            }
        }
        request.getParameters().putAll(param);
        return true;
    }

    /**
     * 요청 URL과 핸들러 클래스의 URL을 비교하고 처음 부분이 일치한다면 그 부분만큼 요청 URL을 잘라내고 반환한다.
     *
     * @return 수정된 요청 URL
     * */
    private String getRequestUrlWithoutHandlerPath() {
        String requestPath = request.getUrl();
        String handlerClassPath = "/";
        if (!requestPath.equals("/")) {
            handlerClassPath += requestPath.split("/")[1];
        }
        if (!this.handlerClassPath.startsWith("/")) {
            this.handlerClassPath = "/" + this.handlerClassPath;
        }
        if (handlerClassPath.equals(this.handlerClassPath)) {
            requestPath = replaceRequestPath(requestPath);
        }
        if (!requestPath.equals("/") && requestPath.endsWith("/")) {
            requestPath = requestPath.substring(0, requestPath.length() - 1);
        }
        return requestPath;
    }

    /**
     * 요청 URL과 핸들러 클래스의 URL을 비교하고 처음 부분이 일치한다면 그 부분만큼 요청 URL을 잘라내고 반환한다.
     *
     * @param requestPath 요청 URL
     * @return 수정된 요청 URL
     * */
    private String replaceRequestPath(String requestPath) {
        int index = requestPath.indexOf(this.handlerClassPath);
        requestPath = requestPath.substring(index + this.handlerClassPath.length());
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        return requestPath;
    }

    /**
     * 핸들러 메서드인지 확인한다.
     *
     * @param annotation 홴들러 메서드의 어노테이션
     * @return 핸들러 메서드인지 여부
     * */
    private boolean isHandlerAnnotation(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(RequestMapping.class) != null;
    }

    /**
     * URL 내에 파라미터가 포함되어 있는지 확인한다.
     *
     * @param url URL
     * @return 파라미터 포함 여부
     * */
    private boolean containsUrlParameter(String url) {
        return url.contains("{") && url.contains("}");
    }

    /**
     * 파라미터에 PathValue 어노테이션이 선언되어 있는지 확인한다.
     *
     * @param parameter 파라미터
     * @return PathValue 어노테이션 선언 여부
     * */
    private boolean isDeclaredPathValueAnnotation(Parameter parameter) {
        return parameter.getDeclaredAnnotation(PathValue.class) != null;
    }

    /**
     * 핸들러 메서드의 Http Method를 확인한다.
     *
     * @param annotation 핸들러 메서드
     * @return 핸들러 메서드의 Http Method
     * */
    private HttpMethod getHandlerHttpMethod(Annotation annotation) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Class<? extends Annotation> handlerAnnotationType = annotation.annotationType();
        Method methodPropertyInAnnotation = handlerAnnotationType.getDeclaredMethod("method");
        return (HttpMethod) methodPropertyInAnnotation.invoke(annotation);
    }

    /**
     * 요청 메서드가 HEAD인지 여부를 반환한다.
     *
     * @param handlerHttpMethod 핸들러 메서드의 Method
     * @return HEAD 메서드인지 여부
     * */
    private boolean isHeadRequest(HttpMethod handlerHttpMethod) {
        return request.getMethod().equals(HttpMethod.HEAD) && HttpMethod.GET.equals(handlerHttpMethod);
    }

}
