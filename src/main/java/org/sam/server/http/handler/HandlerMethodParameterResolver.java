package org.sam.server.http.handler;

import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.annotation.handle.RequestParam;
import org.sam.server.http.Session;
import org.sam.server.http.web.request.HttpRequest;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.HttpResponse;
import org.sam.server.http.web.response.Response;
import org.sam.server.util.Converter;
import org.sam.server.util.PrimitiveWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 핸들러 메서드의 파라미터를 생성하는 클래스
 * */
public class HandlerMethodParameterResolver {

    private static HandlerMethodParameterResolver INSTANCE;

    private HandlerMethodParameterResolver() {}

    public static HandlerMethodParameterResolver getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HandlerMethodParameterResolver();
        }
        return INSTANCE;
    }

    /**
     * 핸들러 메서드에 필요한 파라미터 인스턴스를 생성한다.
     *
     * @param handlerParameters 핸들러 메서드의 파라미터
     * @param request 요청 객체
     * @param response 응답 객체
     * @return 파라미터 인스턴스 목록
     * */
    public Object[] createParameters(Parameter[] handlerParameters, Request request, Response response) {
        List<Object> inputParameters = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            Object parameter = getParameter(handlerParameter, request, response);
            inputParameters.add(parameter);
        }
        return inputParameters.toArray();
    }

    /**
     * 파라미터를 가져온다.
     *
     * @param handlerParameter 핸들러 메서드의 파라미터
     * @param request 요청 객체
     * @param response 응답 객체
     * @return 파라미터 인스턴스
     * */
    private Object getParameter(Parameter handlerParameter, Request request, Response response) {
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
     * 핸들러 메서드의 파라미터를 생성한다.
     *
     * @param value 요청으로 부터 받은 데이터
     * @param type 핸들러 파라미터의 타입
     * @return 파라미터 인스턴스
     * */
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
