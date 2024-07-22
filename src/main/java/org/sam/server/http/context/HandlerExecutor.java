package org.sam.server.http.context;

import org.sam.server.annotation.CrossOrigin;
import org.sam.server.annotation.ExceptionResponse;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.annotation.handle.RequestParam;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.Handler;
import org.sam.server.http.*;
import org.sam.server.http.web.HttpExceptionHandler;
import org.sam.server.http.web.request.HttpRequest;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.HttpResponse;
import org.sam.server.http.web.response.Response;
import org.sam.server.http.web.response.ResponseEntity;
import org.sam.server.util.Converter;
import org.sam.server.util.PrimitiveWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 핸들러를 실행 시키는 클래스
 *
 * @author hypernova1
 * @see HandlerExecutor
 */
public class HandlerExecutor {

    private final Request request;
    private final Response response;
    private final BeanContainer beanContainer = BeanContainer.getInstance();

    private HandlerExecutor(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 인스턴스를 생성한다.
     *
     * @param request  요청 인스턴스
     * @param response 응답 인스턴스
     * @return 인스턴스
     */
    public static HandlerExecutor of(Request request, Response response) {
        return new HandlerExecutor(request, response);
    }

    /**
     * 핸들러를 실행한다.
     *
     * @param handler 핸들러
     */
    public void execute(Handler handler) {
        setCrossOriginConfig(handler);

        try {
            HttpStatus httpStatus;
            Object returnValue;

            try {
                returnValue = executeHandlerWithInterceptor(handler);
            } catch (RuntimeException e) {
                returnValue = throwToResponseEntity(e.getCause().getCause());
            }

            if (returnValue != null && returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            } else {
                httpStatus = HttpStatus.OK;
            }

            String json = "";
            if (returnValue != null) {
                json = Converter.objectToJson(returnValue);
            }

            response.setContentMimeType(ContentType.APPLICATION_JSON);
            response.execute(json, httpStatus);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.badRequest();
        }
    }

    /**
     * 핸들러에서 발생한 예외를 응답으로 변환한다.
     *
     * @param e 핸들러에서 발생한 예외
     * */
    private ResponseEntity<?> throwToResponseEntity(Throwable e) {
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
     * 선언된 예외처리 클래스를 실행한다. 핸들링할 메서드가 없을시 Internal Server Error를 반환한다.
     *
     * @param cause 핸들러에서 발생한 예외
     * @return 응답 데이터
     * */
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
                        return method.invoke(exceptionHandler,cause);
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
     *  예외 핸들링 메서드 중 예외의 최근 부모 클레스를 핸들링하는 메서드를 가져온다.
     *
     * @param cause 예외
     * @param methods 예외 핸들링 메서드 목록
     * @return 핸들링 메서드
     * */
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
     * 예외 핸들링 메서드에 선언된 처리할 익셉션 클래스를 가져온다.
     *
     * @param method 예외 핸들링 메서드
     * */
    private static Class<?> getExceptionClass(Method method) {
        Annotation annotation = method.getDeclaredAnnotation(ExceptionResponse.class);
        try {
            Method annotationMethod = annotation.annotationType().getDeclaredMethod("value");
            return (Class<?>) annotationMethod.invoke(annotation);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 핸들러를 실행시킨 후 리턴 값을 받아온다. interceptor가 구현되어 있다면 interceptor 실행 후 리턴 값을 받아온다.
     *
     * @param handler 핸들러 정보
     * @return 핸들러의 리턴 값
     */
    private Object executeHandlerWithInterceptor(Handler handler) {
        List<Interceptor> interceptors = this.beanContainer.getInterceptors();

        for (Interceptor interceptor : interceptors) {
            interceptor.preHandler(request, response);
        }

        Object returnValue = executeHandler(handler);

        if (!interceptors.isEmpty()) {
            for (int i = interceptors.size() - 1; i >= 0; i--) {
                interceptors.get(i).postHandler(request, response);
            }
        }
        return returnValue;
    }

    /**
     * 핸들러 클래스의 CrossOrigin 어노테이션을 확인하고 CORS를 설정 한다.
     *
     * @param handler 핸들러 정보
     **/
    private void setCrossOriginConfig(Handler handler) {
        Class<?> handlerClass = handler.getHandleInstance().getClass();
        String origin = request.getHeader("origin");

        if (origin == null) return;

        CrossOrigin crossOrigin = handlerClass.getDeclaredAnnotation(CrossOrigin.class);

        if (crossOrigin == null) return;

        String[] value = crossOrigin.value();
        List<String> allowPaths = Arrays.asList(value);
        if (allowPaths.contains("*")) {
            response.setHeader("Access-Control-Allow-Origin", "*");
        } else if (allowPaths.contains(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
    }

    /**
     * 핸들러를 실행하고 반환 값을 반환한다.
     *
     * @param handler 핸들러 정보
     * @return 핸들러의 반환 값
     */
    private Object executeHandler(Handler handler) {
        Method handlerMethod = handler.getMethod();
        Object[] parameters = getParameters(handlerMethod.getParameters());
        try {
            return handlerMethod.invoke(handler.getHandleInstance(), parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 핸들러 실행시 필요한 파라미터 목록을 생성한다.
     *
     * @param handlerParameters 핸들러 클래스의 파라미터 정보
     * @return 핸들러의 파라미터 목록
     */
    private Object[] getParameters(Parameter[] handlerParameters) {
        List<Object> inputParameters = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            Object parameter = getParameter(handlerParameter);
            inputParameters.add(parameter);
        }
        return inputParameters.toArray();
    }

    /**
     * 핸들러 파라미터를 생성 후 반환한다.
     *
     * @param handlerParameter 핸들러 파라미터 정보
     * @return 생성된 파라미터 인스턴스
     */
    private Object getParameter(Parameter handlerParameter) {
        Map<String, String> requestData = request.getParameters();
        String parameterName = handlerParameter.getName();
        Class<?> type = handlerParameter.getType();
        if (HttpRequest.class.isAssignableFrom(type)) {
            return request;
        }
        if (HttpResponse.class.equals(type)) {
            return response;
        }
        if (Session.class.equals(type)) {
            return request.getSession();
        }
        if (handlerParameter.getDeclaredAnnotation(JsonRequest.class) != null) {
            return Converter.jsonToObject(request.getJson(), type);
        }

        Object value = requestData.get(parameterName);
        RequestParam requestParamAnnotation = handlerParameter.getDeclaredAnnotation(RequestParam.class);
        if (requestParamAnnotation != null && value == null) {
            return createParameter(requestParamAnnotation.defaultValue(), type);
        }

        if (value != null) {
            return createParameter(value, type);
        }

        return Converter.parameterToObject(request.getParameters(), type);
    }

    /**
     * 핸들러 실행시 필요한 파라미터를 생성한다.
     *
     * @param value 값
     * @param type  타입
     * @return 핸들러 파라미터
     */
    private Object createParameter(Object value, Class<?> type) {
        if (type.isPrimitive()) {
            return PrimitiveWrapper.wrapPrimitiveValue(type, value.toString());
        } else if (type.getSuperclass().equals(Number.class)) {
            try {
                return type.getMethod("valueOf", String.class).invoke(null, value.toString());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }

}
