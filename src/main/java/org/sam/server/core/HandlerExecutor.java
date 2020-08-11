package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.common.Converter;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            Map<String, String> requestData;
            if (httpRequest.getMethod().equals(HttpMethod.POST) || httpRequest.getMethod().equals(HttpMethod.PUT))
                requestData = httpRequest.getAttributes();
            else
                requestData = httpRequest.getParameters();
            List<Interceptor> interceptors = BeanContainer.getInterceptors();
            interceptors.forEach(interceptor -> interceptor.preHandler(httpRequest, httpResponse));
            Object returnValue = executeHandler(requestData);
            interceptors.forEach(interceptor -> interceptor.postHandler(httpRequest, httpResponse));
            HttpStatus httpStatus;
            if (returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            } else {
                httpStatus = HttpStatus.OK;
            }
            String json = gson.toJson(returnValue);
            httpResponse.execute(json, httpStatus);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            httpResponse.badRequest();
        }
    }

    private Object executeHandler(Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
        Object handlerInstance = findHandlerInstance();
        Method handlerMethod = handlerInfo.getHandlerMethod();
        Object[] parameters = createParameters(handlerMethod.getParameters(), requestData, handlerInstance);
        return handlerMethod.invoke(handlerInstance, parameters);
    }

    private Object findHandlerInstance() {
        List<Object> handlerBeans = BeanContainer.getHandlerBeans();
        return handlerBeans.stream()
                .filter(handlerBean -> handlerInfo.getHandlerClass() == handlerBean.getClass())
                .findFirst()
                .orElseThrow(HandlerNotFoundException::new);
    }

    private Object[] createParameters(Parameter[] handlerParameters, Map<String, String> requestData, Object handlerInstance) {
        List<Object> inputParameter = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            String name = handlerParameter.getName();
            Object value = requestData.get(name);

            Class<?> type = handlerParameter.getType();
            if (HttpRequest.class.isAssignableFrom(type)) {
                inputParameter.add(httpRequest);
                continue;
            }
            if (HttpResponse.class.equals(type)) {
                inputParameter.add(httpResponse);
                continue;
            }
            if (Session.class.equals(type)) {
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
                Object object = Converter.parameterToObject(httpRequest.getParameters(), type, handlerInstance);
                inputParameter.add(object);
            }
        }
        return inputParameter.toArray();
    }

    private void addSession(List<Object> params) {
        Set<Cookie> cookies = httpRequest.getCookies();
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("sessionId")) {
                Session session = httpRequest.getSession();
                if (session == null) cookies.remove(cookie);
                else {
                    session.renewAccessTime();
                    params.add(session);
                    return;
                }
            }
        }
        Session session = new Session();
        params.add(session);
    }
}
