package org.sam.server.http;

import com.google.gson.Gson;
import org.sam.server.annotation.CrossOrigin;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.HandlerInfo;
import org.sam.server.util.Converter;
import org.sam.server.util.PrimitiveWrapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

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

    private HandlerExecutor(HttpRequest httpRequest, HttpResponse httpResponse, HandlerInfo handlerInfo) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.handlerInfo = handlerInfo;
    }

    static HandlerExecutor of(HttpRequest httpRequest, HttpResponse httpResponse, HandlerInfo handlerInfo) {
        return new HandlerExecutor(httpRequest, httpResponse, handlerInfo);
    }

    void execute() {
        Class<?> handlerClass = this.handlerInfo.getInstance().getClass();
        String origin = httpRequest.getHeader("origin");
        if (origin != null) {
            setAccessControlAllowOriginHeader(handlerClass, origin);
        }
        try {
            Map<String, String> requestData;
            if (httpRequest.getMethod().equals(HttpMethod.POST) || httpRequest.getMethod().equals(HttpMethod.PUT)) {
                requestData = httpRequest.getAttributes();
            }
            else {
                requestData = httpRequest.getParameters();
            }
            List<Interceptor> interceptors = BeanContainer.getInterceptors();
            Object returnValue;
            if (interceptors.isEmpty())
                returnValue = executeHandler(requestData);
            else
                returnValue = executeInterceptors(interceptors, requestData);
            HttpStatus httpStatus;
            if (returnValue.getClass().equals(ResponseEntity.class)) {
                ResponseEntity<?> responseEntity = (ResponseEntity<?>) returnValue;
                httpStatus = responseEntity.getHttpStatus();
                returnValue = responseEntity.getValue();
            } else {
                httpStatus = HttpStatus.OK;
            }
            String json = gson.toJson(returnValue);
            httpResponse.setContentMimeType(ContentType.APPLICATION_JSON);
            httpResponse.execute(json, httpStatus);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            httpResponse.badRequest();
        }
    }

    private void setAccessControlAllowOriginHeader(Class<?> handlerClass, String origin) {
        CrossOrigin crossOrigin = handlerClass.getDeclaredAnnotation(CrossOrigin.class);
        if (crossOrigin != null) {
            String[] value = crossOrigin.value();
            List<String> allowPaths = Arrays.asList(value);
            if (allowPaths.contains("*")) {
                httpResponse.setHeader("Access-Control-Allow-Origin", "*");
            } else if (allowPaths.contains(origin)) {
                httpResponse.setHeader("Access-Control-Allow-Origin", origin);
            }
        }
    }

    private Object executeInterceptors(List<Interceptor> interceptors, Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
        Object returnValue = null;
        for (Interceptor interceptor : interceptors) {
            interceptor.preHandler(httpRequest, httpResponse);
            returnValue = executeHandler(requestData);
            interceptor.postHandler(httpRequest, httpResponse);
        }
        return returnValue;
    }

    private Object executeHandler(Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
        Method handlerMethod = handlerInfo.getMethod();
        Object[] parameters = createParameters(handlerMethod.getParameters(), requestData);
        return handlerMethod.invoke(handlerInfo.getInstance(), parameters);
    }

    private Object[] createParameters(Parameter[] handlerParameters, Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
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
                } else if (type.getSuperclass().equals(Number.class))  {
                    try {
                        Object wrapperValue = type.getMethod("valueOf", String.class).invoke(null, value);
                        inputParameter.add(wrapperValue);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                } else if (type.equals(String.class)) {
                    inputParameter.add(value);
                }
            } else {
                Object object = Converter.parameterToObject(httpRequest.getParameters(), type);
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
