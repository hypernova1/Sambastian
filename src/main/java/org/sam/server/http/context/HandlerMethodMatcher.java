package org.sam.server.http.context;

import org.sam.server.annotation.handle.PathValue;
import org.sam.server.annotation.handle.RequestMapping;
import org.sam.server.annotation.handle.RestApi;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.Stream;

/**
 * 요청과 일치하는 핸들러 메서드를 찾는 클래스.
 */
public class HandlerMethodMatcher {
    private final Request request;
    private final Response response;
    private String handlerClassPath;

    public HandlerMethodMatcher(Request request, Response response, String handlerClassPath) {
        this.request = request;
        this.response = response;
        this.handlerClassPath = handlerClassPath;
    }

    /**
     * 핸들러 클래스에서 핸들러 메서드를 찾는다.
     *
     * @param handlerMethods 핸들러 클래스 내부에 선언된 메서드 목록
     * @param pathValueHandlerMethods url 파라미터 핸들러 메서드 목록
     * @return 핸들러 메서드
     * */
    public Optional<Method> findHandlerMethod(List<Method> handlerMethods, List<Method> pathValueHandlerMethods) {
        return Stream.of(
                        findHandlerMethod(handlerMethods),
                        findHandlerMethod(pathValueHandlerMethods)
                )
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    /**
     * 핸들러 메서드를 찾는다.
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
     * 핸들러 메서드와 요청이 일치하는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @return 일치 여부
     * */
    private boolean isMatchHandlerMethod(Method handlerMethod) {
        Annotation[] annotations = handlerMethod.getDeclaredAnnotations();
        return Arrays.stream(annotations)
                .filter(this::isHandlerAnnotation)
                .anyMatch(annotation -> isMatchedHandlerMethod(handlerMethod, annotation));
    }

    /**
     * 핸들러 메서드와 요청이 일치하는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param annotation 핸들러 메서드의 어노테이션
     * @return 일치 여부
     * */
    private boolean isMatchedHandlerMethod(Method handlerMethod, Annotation annotation) {
        try {
            String requestUrl = getRequestUrlWithoutHandlerPath();
            String handlerMethodPath = getHandlerMethodPath(annotation);
            if (requestUrl.equals(request.getUrl())) {
                if (this.handlerClassPath.equals("/")) {
                    this.handlerClassPath = "";
                }
                handlerMethodPath = this.handlerClassPath + handlerMethodPath;
            }

            HttpMethod handlerHttpMethod = getHandlerHttpMethod(annotation);
            if (isMatchedHandlerMethod(handlerMethod, handlerMethodPath, handlerHttpMethod)) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * 핸들러 메서드의 경로를 가져온다.
     *
     * @param annotation 핸들러 메서드에 선언된 어노테이션
     * @return 경로
     * */
    private String getHandlerMethodPath(Annotation annotation) throws Exception {
        Method pathPropertyInAnnotation = annotation.annotationType().getDeclaredMethod("value");
        String handlerMethodPath = pathPropertyInAnnotation.invoke(annotation).toString();
        if (!handlerMethodPath.startsWith("/")) {
            handlerMethodPath = "/" + handlerMethodPath;
        }
        return handlerMethodPath;
    }

    /**
     * 핸들러 메서드가 요청과 일치하는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerMethodPath 핸들러 메서드 경로
     * @param handlerHttpMethod 핸들러 메서드 HTTP 메서드
     * @return 일치 여부
     */
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
     * 요청 경로와 핸들러 메서드 경로가 일치하는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerMethodPath 핸들러 메서드 경로
     * @return 일치 여부
     */
    private boolean isMatchedPath(Method handlerMethod, String handlerMethodPath) {
        String requestPath = getRequestUrlWithoutHandlerPath();

        if (existsPathValueAnnotation(handlerMethod)) {
            Queue<String> paramNames = HandlerPathMatcher.extractPathVariables(handlerMethodPath);
            return HandlerPathMatcher.isMatchedPath(requestPath, handlerMethodPath, paramNames, request.getParameters());
        }
        return requestPath.equals(handlerMethodPath);
    }

    /**
     * 핸들러 메서드에 PathValue 어노테이션이 선언되어 있는지 확인한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @return 선언 여부
     */
    private boolean existsPathValueAnnotation(Method handlerMethod) {
        return Arrays.stream(handlerMethod.getParameters())
                .anyMatch(this::isDeclaredPathValueAnnotation);
    }

    /**
     * 핸들러 클래스 경로를 제외한 요청 URL을 반환한다.
     *
     * @return 요청 URL
     */
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
     * 요청 경로에서 핸들러 클래스 경로를 제거한다.
     *
     * @param requestPath 요청 경로
     * @return 핸들러 클래스 경로를 제거한 요청 경로
     */
    private String replaceRequestPath(String requestPath) {
        int index = requestPath.indexOf(this.handlerClassPath);
        requestPath = requestPath.substring(index + this.handlerClassPath.length());
        if (!requestPath.startsWith("/")) requestPath = "/" + requestPath;
        return requestPath;
    }

    /**
     * 주어진 어노테이션이 RequestMapping 어노테이션인지 확인한다.
     *
     * @param annotation 어노테이션
     * @return 확인 여부
     */
    private boolean isHandlerAnnotation(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(RequestMapping.class) != null;
    }

    /**
     * 주어진 파라미터에 PathValue 어노테이션이 선언되어 있는지 확인한다.
     *
     * @param parameter 파라미터
     * @return 선언 여부
     */
    private boolean isDeclaredPathValueAnnotation(Parameter parameter) {
        return parameter.getDeclaredAnnotation(PathValue.class) != null;
    }

    /**
     * 핸들러 메서드의 HTTP 메서드를 반환한다.
     *
     * @param annotation 핸들러 메서드 어노테이션
     * @return HTTP 메서드
     */
    private HttpMethod getHandlerHttpMethod(Annotation annotation) throws Exception {
        Method methodPropertyInAnnotation = annotation.annotationType().getDeclaredMethod("method");
        return (HttpMethod) methodPropertyInAnnotation.invoke(annotation);
    }

    /**
     * 요청 메서드가 HEAD 요청인지 확인한다.
     *
     * @param handlerHttpMethod 핸들러 메서드 HTTP 메서드
     * @return 확인 여부
     */
    private boolean isHeadRequest(HttpMethod handlerHttpMethod) {
        return request.getMethod().equals(HttpMethod.HEAD) && HttpMethod.GET.equals(handlerHttpMethod);
    }
}
