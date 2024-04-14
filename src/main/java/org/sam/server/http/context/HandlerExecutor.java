package org.sam.server.http.context;

import org.sam.server.annotation.CrossOrigin;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.Handler;
import org.sam.server.http.*;
import org.sam.server.http.web.request.HttpRequest;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.HttpResponse;
import org.sam.server.http.web.response.Response;
import org.sam.server.http.web.response.ResponseEntity;
import org.sam.server.util.Converter;
import org.sam.server.util.PrimitiveWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * 핸들러를 실행 시키는 클래스입니다.
 *
 * @author hypernova1
 * @see HandlerExecutor
 * */
public class HandlerExecutor {

    private final Request request;
    private final Response response;
    private final BeanContainer beanContainer = BeanContainer.getInstance();

    private HandlerExecutor(Request request, Response response) {
        this.request = request;
        this.response = response;
    }

    /**
     * 인스턴스를 생성합니다.
     * 
     * @param request 요청 인스턴스
     * @param response 응답 인스턴스
     * @return 인스턴스
     * */
    public static HandlerExecutor of(Request request, Response response) {
        return new HandlerExecutor(request, response);
    }

    /**
     * 핸들러를 실행합니다.
     * */
    public void execute(Handler handlerInfo) {
        setCrossOriginConfig(handlerInfo);
        SessionManager.removeExpiredSession();
        try {
            Object returnValue = executeHandlerWithInterceptor(handlerInfo);
            HttpStatus httpStatus;
            if (returnValue != null && returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            } else {
                httpStatus = HttpStatus.OK;
            }
            String json = Converter.objectToJson(returnValue);
            response.setContentMimeType(ContentType.APPLICATION_JSON);
            response.execute(json, httpStatus);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.badRequest();
        }
    }

    /**
     * 핸들러를 실행시킨 후 리턴 값을 받아옵니다. interceptor가 구현되어 있다면 interceptor 실행 후 리턴 값을 받아옵니다.
     *
     * @param handler 핸들러 정보
     * @return 핸들러의 리턴 값
     * */
    private Object executeHandlerWithInterceptor(Handler handler) {
        List<Interceptor> interceptors = beanContainer.getInterceptors();

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
     * 핸들러 클래스의 CrossOrigin 어노테이션을 확인하고 CORS를 설정 합니다.
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
     * 핸들러를 실행하고 반환 값을 반환합니다.
     *
     * @param handler 핸들러 정보
     * @return 핸들러의 반환 값
     * */
    private Object executeHandler(Handler handler) {
        Method handlerMethod = handler.getMethod();
        Object[] parameters = getParameters(handlerMethod.getParameters());
        try {
            return handlerMethod.invoke(handler.getHandleInstance(), parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 핸들러 실행시 필요한 파라미터 목록을 생성합니다.
     * 
     * @param handlerParameters 핸들러 클래스의 파라미터 정보
     * @return 핸들러의 파라미터 목록
     * */
    private Object[] getParameters(Parameter[] handlerParameters) {
        List<Object> inputParameters = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            Object parameter = getParameter(handlerParameter);
            inputParameters.add(parameter);
        }
        return inputParameters.toArray();
    }

    /**
     * 핸들러 파라미터를 생성 후 반환합니다.
     *
     * @param handlerParameter 핸들러 파라미터 정보
     * @return 생성된 파라미터 인스턴스
     * */
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
        if (value != null) {
            return createParameter(value, type);
        }
        return Converter.parameterToObject(request.getParameters(), type);
    }

    /**
     * 핸들러 실행시 필요한 파라미터를 생성합니다.
     *
     * @param value 값
     * @param type 타입
     * @return 핸들러 파라미터
     * */
    private Object createParameter(Object value, Class<?> type) {
        if (type.isPrimitive()) {
            return PrimitiveWrapper.wrapPrimitiveValue(type, value.toString());
        } else if (type.getSuperclass().equals(Number.class))  {
            try {
                return type.getMethod("valueOf", String.class).invoke(null, value.toString());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        return value;
    }

}
