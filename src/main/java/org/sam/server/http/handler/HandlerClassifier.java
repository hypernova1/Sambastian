package org.sam.server.http.handler;

import org.sam.server.annotation.handle.RequestMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 핸들러를 분류하는 클래스
 *   URL 파라미터가 있는 핸들러와 일반 핸들러를 분류한다.
 * */
public class HandlerClassifier {

    private final List<Method> pathValueHandlerMethods = new ArrayList<>();
    private final List<Method> handlerMethods = new ArrayList<>();

    /**
     * 주어진 핸들러 클래스를 분류한다.
     *
     * @param handlerClass 핸들러 클래스
     */
    public void classifyHandler(Class<?> handlerClass) {
        Method[] methods = handlerClass.getDeclaredMethods();
        for (Method method : methods) {
            classifyHandlerMethod(method);
        }
    }

    /**
     * 주어진 핸들러 메서드를 분류한다.
     *
     * @param handlerMethod 핸들러 메서드
     */
    private void classifyHandlerMethod(Method handlerMethod) {
        for (Annotation annotation : handlerMethod.getDeclaredAnnotations()) {
            if (!isHandlerAnnotation(annotation)) continue;
            classifyHandler(handlerMethod, annotation);
        }
    }

    /**
     * 핸들러 메서드를 URL 파라미터 여부에 따라 분류하여 목록에 추가한다.
     *
     * @param handlerMethod 핸들러 메서드
     * @param handlerAnnotation 핸들러 메서드의 어노테이션
     */
    private void classifyHandler(Method handlerMethod, Annotation handlerAnnotation) {
        try {
            Method value = handlerAnnotation.annotationType().getMethod("value");
            String path = String.valueOf(value.invoke(handlerAnnotation));
            if (HandlerPathMatcher.containsUrlParameter(path)) {
                pathValueHandlerMethods.add(handlerMethod);
                return;
            }
            handlerMethods.add(handlerMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * URL 파라미터가 있는 핸들러 메서드 목록을 반환한다.
     *
     * @return URL 파라미터가 있는 핸들러 메서드 목록
     */
    public List<Method> getPathValueHandlerMethods() {
        return pathValueHandlerMethods;
    }

    /**
     * 일반 핸들러 메서드 목록을 반환한다.
     *
     * @return 일반 핸들러 메서드 목록
     */
    public List<Method> getHandlerMethods() {
        return handlerMethods;
    }

    /**
     * 주어진 애노테이션이 핸들러 애노테이션인지 확인한다.
     *
     * @param annotation 애노테이션
     * @return 핸들러 애노테이션 여부
     */
    private boolean isHandlerAnnotation(Annotation annotation) {
        return annotation.annotationType().getDeclaredAnnotation(RequestMapping.class) != null;
    }

}
