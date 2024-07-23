package org.sam.server.http.context;

import org.sam.server.annotation.ExceptionResponse;
import org.sam.server.constant.HttpStatus;
import org.sam.server.context.BeanContainer;
import org.sam.server.http.web.HttpExceptionHandler;
import org.sam.server.http.web.response.ResponseEntity;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 예외를 처리하는 Executor 클래스.
 * 예외에 해당하는 핸들러 메서드를 찾아 실행하고, ResponseEntity로 반환한다.
 */
public class ExceptionHandlerExecutor {
    private static ExceptionHandlerExecutor INSTANCE;
    private final BeanContainer beanContainer = BeanContainer.getInstance();

    private ExceptionHandlerExecutor() {}

    public static ExceptionHandlerExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ExceptionHandlerExecutor();
        }
        return INSTANCE;
    }

    /**
     * 예외를 처리하고 ResponseEntity를 반환한다.
     *
     * @param e 발생한 예외
     * @return ResponseEntity 객체
     */
    public ResponseEntity<?> handleException(Throwable e) {
        HttpStatus httpStatus;
        Object returnValue = this.executeExceptionHandler(e);
        if (returnValue instanceof ResponseEntity<?>) {
            return (ResponseEntity<?>) returnValue;
        }

        if (HttpException.class.isAssignableFrom(e.getClass())) {
            httpStatus = ((HttpException) e).getStatus();
        } else {
            returnValue = e.toString();
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ResponseEntity<>(httpStatus, returnValue);
    }

    /**
     * 예외를 처리하는 핸들러 메서드를 찾아 실행한다.
     *
     * @param cause 발생한 예외
     * @return 핸들러 메서드의 반환 값
     */
    private Object executeExceptionHandler(Throwable cause) {
        List<Object> handlerBeans = this.beanContainer.getHandlerBeans();
        List<Object> exceptionHandlers = handlerBeans.stream()
                .filter((handlerBean) -> HttpExceptionHandler.class.isAssignableFrom(handlerBean.getClass()))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Method> sameSuperClassMethods = new ArrayList<>();
        try {
            for (Object exceptionHandler : exceptionHandlers) {
                Method[] methods = exceptionHandler.getClass().getDeclaredMethods();
                for (Method method : methods) {
                    Class<?> exceptionClass = getExceptionClass(method);
                    if (exceptionClass.equals(cause.getClass())) {
                        return method.invoke(exceptionHandler, cause);
                    }

                    sameSuperClassMethods.add(method);
                }
            }

            Method method = this.findSuperException(cause, sameSuperClassMethods);
            if (method != null) {
                Object handlerInstance = this.beanContainer.findHandlerByClass(method.getDeclaringClass());
                return method.invoke(handlerInstance, cause);
            }

            cause.printStackTrace();
            return "Internal Server Error";
        } catch (InvocationTargetException | IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 예외의 부모 클래스에 해당하는 핸들러 메서드를 찾는다.
     *
     * @param cause 예외
     * @param methods 핸들러 메서드 목록
     * @return 핸들러 메서드
     */
    private Method findSuperException(Throwable cause, List<Method> methods) {
        Class<?> causeClass = cause.getClass();

        while (!methods.isEmpty()) {
            Iterator<Method> iterator = methods.iterator();
            causeClass = causeClass.getSuperclass();
            if (causeClass == null) {
                return null;
            }
            while (iterator.hasNext()) {
                Method method = iterator.next();

                Class<?> exceptionClass = getExceptionClass(method);
                if (exceptionClass.equals(causeClass)) {
                    return method;
                }

                if (exceptionClass.equals(Object.class)) {
                    iterator.remove();
                }
            }
        }
        return null;
    }

    /**
     * 핸들러 메서드의 예외 클래스 정보를 반환한다.
     *
     * @param method 핸들러 메서드
     * @return 예외 클래스
     */
    private static Class<?> getExceptionClass(Method method) {
        Annotation annotation = method.getDeclaredAnnotation(ExceptionResponse.class);
        try {
            Method annotationMethod = annotation.annotationType().getDeclaredMethod("value");
            return (Class<?>) annotationMethod.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
}
