package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.HttpServer;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.common.Converter;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.http.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

            Object[] parameters = createParameters(handlerInfo.getHandlerMethod().getParameters(), requestParams).toArray();
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

    private List<Object> createParameters(Parameter[] handlerParameters, Map<String, ?> requestParams) {
        List<Object> inputParameter = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            String name = handlerParameter.getName();
            Object value = requestParams.get(name);

            Class<?> type = handlerParameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)) {
                inputParameter.add(httpRequest);
                continue;
            }
            if (type.equals(HttpResponse.class)) {
                inputParameter.add(httpResponse);
                continue;
            }
            if (type.equals(Session.class)) {
                addSession(inputParameter);
                continue;
            }
            if (handlerParameter.getDeclaredAnnotation(JsonRequest.class) != null) {
                Object object = Converter.jsonToObject(httpRequest.getJson(), type);
                inputParameter.add(object);
                continue;
            }

            if (value != null) {
                if (type.isPrimitive()) {
                    Object autoBoxingValue = PrimitiveWrapper.wrapPrimitiveValue(type, value.toString());
                    inputParameter.add(autoBoxingValue);
                } else if (type.equals(String.class)) {
                    inputParameter.add(value);
                }
            } else {
                Object object = Converter.parameterToObject(httpRequest.getParameters(), type);
                inputParameter.add(object);
            }
        }
        return inputParameter;
    }

    private void addSession(List<Object> params) {
        SessionManager sessionManager = HttpServer.sessionManager;
        Set<Cookie> cookies = httpRequest.getCookies();

        if (cookies.size() == 0) {
            Session session = sessionManager.createSession();
            params.add(session);
            return;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("sessionId")) {
                Session session = sessionManager.getSession(cookie.getValue());
                if (session == null) {
                    cookies.remove(cookie);
                } else {
                    session.renewAccessTime();
                    params.add(session);
                    return;
                }
            }
        }
        Session session = sessionManager.createSession();
        params.add(session);
    }
}
