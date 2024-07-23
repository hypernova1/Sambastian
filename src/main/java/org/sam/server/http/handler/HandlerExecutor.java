package org.sam.server.http.handler;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.bean.Handler;
import org.sam.server.http.context.CorsConfigHandler;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;
import org.sam.server.http.web.response.ResponseEntity;
import org.sam.server.util.Converter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 핸들러를 실행 시키는 클래스
 *
 * @author hypernova1
 * @see HandlerExecutor
 */
public class HandlerExecutor {

    private static HandlerExecutor INSTANCE;

    private final ExceptionHandlerExecutor exceptionHandlerExecutor;
    private final CorsConfigHandler corsConfigHandler;
    private final InterceptorExecutor interceptorExecutor;
    private final HandlerMethodParameterResolver handlerMethodParameterResolver;

    private HandlerExecutor() {
        this.exceptionHandlerExecutor = ExceptionHandlerExecutor.getInstance();
        this.interceptorExecutor = InterceptorExecutor.getInstance();
        this.corsConfigHandler = CorsConfigHandler.getInstance();
        this.handlerMethodParameterResolver = HandlerMethodParameterResolver.getInstance();
    }

    public static HandlerExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HandlerExecutor();
        }
        return INSTANCE;
    }

    /**
     * 핸들러를 실행하고 클라이언트에 응답한다.
     *
     * @param handler 핸들러
     * @param request 요청
     * @param response 응답
     * */
    public void execute(Handler handler, Request request, Response response) {
        corsConfigHandler.configureCors(handler.getHandleInstance(), request, response);

        try {
            HttpStatus httpStatus;
            Object returnValue;

            try {
                returnValue = executeHandlerWithInterceptor(handler, request, response);
            } catch (RuntimeException e) {
                returnValue = exceptionHandlerExecutor.handleException(e.getCause().getCause());
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
     * 핸들러 전후로 인터셉터를 실행한다.
     *
     * @param handler 핸들러
     * @param request 요청
     * @param response 응답
     * @return 반환 값
     * */
    private Object executeHandlerWithInterceptor(Handler handler, Request request, Response response) {
        interceptorExecutor.executePreHandlers(request, response);

        Object returnValue = executeHandler(handler, request, response);

        interceptorExecutor.executePostHandlers(request, response);
        return returnValue;
    }

    /**
     * 핸들러를 실행한다.
     *
     * @param handler 핸들러
     * @param request 요청
     * @param response 응답
     * @return 반환 값
     * */
    private Object executeHandler(Handler handler, Request request, Response response) {
        Method handlerMethod = handler.getMethod();
        Object[] parameters = handlerMethodParameterResolver.createParameters(handlerMethod.getParameters(), request, response);
        try {
            return handlerMethod.invoke(handler, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
