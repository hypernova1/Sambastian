package org.sam.server.http;

import com.google.gson.Gson;
import org.sam.server.annotation.CrossOrigin;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.constant.ContentType;
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
 * 핸들러를 실행 시키는 클래스입니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.HandlerExecutor
 * */
public class HandlerExecutor {

    private final HttpResponse httpResponse;

    private final HttpRequest httpRequest;

    private final HandlerInfo handlerInfo;

    private final Gson gson;
    
    private HandlerExecutor(HttpRequest httpRequest, HttpResponse httpResponse, HandlerInfo handlerInfo) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.handlerInfo = handlerInfo;
        this.gson = new Gson();
    }

    /**
     * 인스턴스를 생성합니다.
     * 
     * @param httpRequest 요청 인스턴스
     * @param httpResponse 응답 인스턴스
     * @param handlerInfo 핸들러 정보
     * @return 인스턴스
     * */
    static HandlerExecutor of(HttpRequest httpRequest, HttpResponse httpResponse, HandlerInfo handlerInfo) {
        return new HandlerExecutor(httpRequest, httpResponse, handlerInfo);
    }

    /**
     * 핸들러를 실행합니다.
     * */
    void execute() {
        setCrossOriginConfig();
        try {
            Map<String, String> requestData = httpRequest.getParameters();
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

    /**
     * CORS를 설정합니다.
     * */
    private void setCrossOriginConfig() {
        Class<?> handlerClass = this.handlerInfo.getInstance().getClass();
        String origin = httpRequest.getHeader("origin");
        if (origin != null)
            setAccessControlAllowOriginHeader(handlerClass, origin);
    }

    /**
     * 핸들러 클래스의 CrossOrigin 어노테이션을 확인하고 CORS를 설정 합니다.
     *
     * @param handlerClass 핸들러 클래스의 정보
     * @param origin 허용 URL
     * */
    private void setAccessControlAllowOriginHeader(Class<?> handlerClass, String origin) {
        CrossOrigin crossOrigin = handlerClass.getDeclaredAnnotation(CrossOrigin.class);
        if (crossOrigin != null) {
            String[] value = crossOrigin.value();
            List<String> allowPaths = Arrays.asList(value);
            if (allowPaths.contains("*"))
                httpResponse.setHeader("Access-Control-Allow-Origin", "*");
            else if (allowPaths.contains(origin))
                httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        }
    }

    /**
     * 인터셉터를 실행하고 핸들러를 실행하여 핸들러의 반환 값을 반환합니다.
     *
     * @param interceptors 인터셉터 목록
     * @param requestData 요청 데이터
     * @return 핸들러의 반환 값
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * */
    private Object executeInterceptors(List<Interceptor> interceptors, Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
        Object returnValue = null;
        for (Interceptor interceptor : interceptors) {
            interceptor.preHandler(httpRequest, httpResponse);
            returnValue = executeHandler(requestData);
            interceptor.postHandler(httpRequest, httpResponse);
        }
        return returnValue;
    }

    /**
     * 핸들러를 실행하고 반환 값을 반환합니다.
     *
     * @param 요청 파라미터 목록
     * @return 핸들러의 반환 값
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * */
    private Object executeHandler(Map<String, String> requestData) throws IllegalAccessException, InvocationTargetException {
        Method handlerMethod = handlerInfo.getMethod();
        Object[] parameters = createParameters(handlerMethod.getParameters(), requestData);
        return handlerMethod.invoke(handlerInfo.getInstance(), parameters);
    }

    /**
     * 핸들러 실행시 필요한 파라미터 목록을 생성합니다.
     * 
     * @param handlerParameters 핸들러 클래스의 파라미터 정보
     * @param requestData 요청 파라미터 목록
     * @return 핸들러의 파라미터 목록
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * */
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

    /**
     * 핸들러 실행시 필요한 세션을 파라미터에 추가합니다.
     *
     * @param params 핸들러의 파라미터 목록
     * */
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
