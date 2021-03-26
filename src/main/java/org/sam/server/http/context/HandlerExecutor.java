package org.sam.server.http.context;

import org.sam.server.annotation.CrossOrigin;
import org.sam.server.annotation.handle.JsonRequest;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.context.BeanContainer;
import org.sam.server.context.HandlerInfo;
import org.sam.server.http.*;
import org.sam.server.http.web.*;
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
    public void execute(HandlerInfo handlerInfo) {
        setCrossOriginConfig(handlerInfo);
        SessionManager.checkExpiredSession();
        try {
            Object returnValue = executeHandler(handlerInfo);
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
     * @param handlerInfo 핸들러 정보
     * @return 핸들러의 리턴 값
     * */
    private Object executeHandler(HandlerInfo handlerInfo) {
        Map<String, String> requestData = request.getParameters();
        List<Interceptor> interceptors = BeanContainer.getInterceptors();
        if (interceptors.isEmpty()) {
            return executeHandler(handlerInfo, requestData);
        }
        return executeInterceptorAndHandler(interceptors, handlerInfo, requestData);
    }

    /**
     * 핸들러 클래스의 CrossOrigin 어노테이션을 확인하고 CORS를 설정 합니다.
     *
     * @param handlerInfo 핸들러 정보
     **/
    private void setCrossOriginConfig(HandlerInfo handlerInfo) {
        Class<?> handlerClass = handlerInfo.getInstance().getClass();
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
     * 인터셉터를 실행하고 핸들러를 실행하여 핸들러의 반환 값을 반환합니다.
     *
     * @param interceptors 인터셉터 목록
     * @param requestData 요청 데이터
     * @return 핸들러의 반환 값
     * */
    private Object executeInterceptorAndHandler(List<Interceptor> interceptors, HandlerInfo handlerInfo, Map<String, String> requestData) {
        for (Interceptor interceptor : interceptors) {
            interceptor.preHandler(request, response);
        }

        Object returnValue = executeHandler(handlerInfo, requestData);

        Collections.reverse(interceptors);
        for (Interceptor interceptor : interceptors) {
            interceptor.postHandler(request, response);
        }
        return returnValue;
    }

    /**
     * 핸들러를 실행하고 반환 값을 반환합니다.
     *
     * @param handlerInfo 핸들러 정보
     * @param requestData 요청 파라미터 목록
     * @return 핸들러의 반환 값
     * */
    private Object executeHandler(HandlerInfo handlerInfo, Map<String, String> requestData) {
        Method handlerMethod = handlerInfo.getMethod();
        Object[] parameters = createParameters(handlerMethod.getParameters(), requestData);
        try {
            return handlerMethod.invoke(handlerInfo.getInstance(), parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 핸들러 실행시 필요한 파라미터 목록을 생성합니다.
     * 
     * @param handlerParameters 핸들러 클래스의 파라미터 정보
     * @param requestData 요청 파라미터 목록
     * @return 핸들러의 파라미터 목록
     * */
    private Object[] createParameters(Parameter[] handlerParameters, Map<String, String> requestData) {
        List<Object> inputParameter = new ArrayList<>();
        for (Parameter handlerParameter : handlerParameters) {
            setParameter(requestData, inputParameter, handlerParameter);
        }
        return inputParameter.toArray();
    }

    /**
     * 핸들러 파라미터를 생성하고 파라미터 리스트에 추가합니다.
     *
     * @param requestData 요청 파라미터
     * @param inputParameter 핸들러 파라미터의 인스턴스를 담을 리스트
     * @param handlerParameter 핸들러 파라미터 정보
     * */
    private void setParameter(Map<String, String> requestData, List<Object> inputParameter, Parameter handlerParameter) {
        String name = handlerParameter.getName();
        Object value = requestData.get(name);
        Class<?> type = handlerParameter.getType();
        if (HttpRequest.class.isAssignableFrom(type)) {
            inputParameter.add(request);
            return;
        }
        if (HttpResponse.class.equals(type)) {
            inputParameter.add(response);
            return;
        }
        if (Session.class.equals(type)) {
            addSession(inputParameter);
            return;
        }
        if (handlerParameter.getDeclaredAnnotation(JsonRequest.class) != null) {
            Object object = Converter.jsonToObject(request.getJson(), type);
            inputParameter.add(object);
            return;
        }
        Object object;
        if (value != null) {
            object = setParameter(value, type);
        } else {
            object = Converter.parameterToObject(request.getParameters(), type);
        }
        inputParameter.add(object);
    }

    /**
     * 핸들러 실행시 필요한 파라미터를 생성합니다.
     *
     * @param value 값
     * @param type 타입
     * @return 핸들러 파라미터
     * */
    private Object setParameter(Object value, Class<?> type) {
        if (type.isPrimitive()) {
            return PrimitiveWrapper.wrapPrimitiveValue(type, value.toString());
        } else if (type.getSuperclass().equals(Number.class))  {
            try {
                return type.getMethod("valueOf", String.class).invoke(null, value.toString());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 핸들러 실행시 필요한 세션을 파라미터에 추가합니다.
     *
     * @param params 핸들러의 파라미터 목록
     * */
    private void addSession(List<Object> params) {
        Set<Cookie> cookies = request.getCookies();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (!cookie.getName().equals("sessionId")) continue;

            Session session = request.getSession();
            if (session != null) {
                session.renewAccessTime();
                params.add(session);
                return;
            }
            iterator.remove();
        }
        Session session = new Session();
        params.add(session);
    }
}
