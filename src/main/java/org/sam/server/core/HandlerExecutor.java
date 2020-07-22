package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.common.Converter;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.http.HttpMultipartRequest;
import org.sam.server.http.HttpRequest;
import org.sam.server.http.HttpResponse;
import org.sam.server.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 10:35 AM
 */
public class HandlerExecutor {
    private final HttpResponse httpResponse;
    private final HttpRequest httpRequest;
    private final HandlerInfo handlerInfo;
    private final Gson gson = new Gson();

    public HandlerExecutor(HttpRequest httpRequest, HttpResponse httpResponse, HandlerInfo handlerInfo) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.handlerInfo = handlerInfo;
    }

    public void execute() {
        try {
            Map<String, ?> requestParams;
            if (httpRequest.getMethod().equals(HttpMethod.POST) || httpRequest.getMethod().equals(HttpMethod.PUT)) {
                requestParams = httpRequest.getAttributes();
            } else {
                requestParams = httpRequest.getParameters();
            }

            Object[] parameters = getParameters(handlerInfo.getHandlerMethod().getParameters(), requestParams).toArray();
            Object returnValue = handlerInfo.getHandlerMethod().invoke(handlerInfo.getHandlerClass().newInstance(), parameters);
            HttpStatus httpStatus = HttpStatus.OK;
            if (returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            }

            String json = gson.toJson(returnValue);
            httpResponse.execute(json, httpStatus);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            httpResponse.badRequest();
        }
    }

    private List<Object> getParameters(Parameter[] parameters, Map<String, ?> requestParams) {
        List<Object> params = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            Object value = requestParams.get(name);

            Class<?> type = parameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)) {
                params.add(httpRequest);
                continue;
            }
            if (type.equals(HttpResponse.class)) {
                params.add(httpResponse);
                continue;
            }
            if (parameter.getDeclaredAnnotation(JsonRequest.class) != null) {
                Object object = Converter.jsonToObject(httpRequest.getJson(), type);
                params.add(object);
                continue;
            }

            if (value != null) {
                if (type.isPrimitive()) {
                    Object autoBoxingValue = PrimitiveWrapper.wrapPrimitiveValue(type, value.toString());
                    params.add(autoBoxingValue);
                } else if (type.equals(String.class)) {
                    params.add(value);
                }
            } else {
                Object object = Converter.parameterToObject(httpRequest.getParameters(), type);
                params.add(object);
            }
        }
        return params;
    }
}
