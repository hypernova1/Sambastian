package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.common.Converter;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.HttpStatus;
import org.sam.server.http.Request;
import org.sam.server.http.Response;
import org.sam.server.http.ResponseEntity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 10:35 AM
 */
public class HandlerExecutor {
    private final Response response;
    private final Request request;
    private final HandlerInfo handlerInfo;
    private final Gson gson = new Gson();

    public HandlerExecutor(Request request, Response response, HandlerInfo handlerInfo) {
        this.request = request;
        this.response = response;
        this.handlerInfo = handlerInfo;
    }

    public void execute() {
        Object[] parameters = getMethodParameters(handlerInfo.getHandlerMethod().getParameters()).toArray();
        Object returnValue;
        try {
            returnValue = handlerInfo.getHandlerMethod().invoke(handlerInfo.getHandlerClass().newInstance(), parameters);
            HttpStatus httpStatus = HttpStatus.OK;
            if (returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = ResponseEntity.class.cast(returnValue);
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            }

            String json = gson.toJson(returnValue);
            response.execute(json, httpStatus);
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            response.badRequest();
        }
    }

    private List<Object> getMethodParameters(Parameter[] parameters) {
        List<Object> params = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            String value = request.getParameter(name);

            Class<?> type = parameter.getType();
            if (type.equals(Request.class)) {
                params.add(request);
                continue;
            }
            if (type.equals(Response.class)) {
                params.add(response);
                continue;
            }

            if (parameter.getDeclaredAnnotation(JsonRequest.class) != null) {
                Object object = Converter.jsonToObject(request.getJson(), type);
                params.add(object);
                continue;
            }

            if (value != null) {
                if (type.isPrimitive()) {
                    Object autoBoxingValue = PrimitiveWrapper.wrapPrimitiveValue(type, value);
                    params.add(autoBoxingValue);
                } else if (type.equals(String.class)) {
                    params.add(value);
                }
            } else {
                Object object = Converter.parameterToObject(request.getParameters(), type);
                params.add(object);
            }
        }
        return params;
    }
}
