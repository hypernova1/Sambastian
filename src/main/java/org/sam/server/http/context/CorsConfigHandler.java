package org.sam.server.http.context;

import org.sam.server.annotation.CrossOrigin;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

import java.util.Arrays;
import java.util.List;

/**
 * CORS 관련 설정 클래스
 * */
public class CorsConfigHandler {

    private static CorsConfigHandler INSTANCE;

    public static CorsConfigHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CorsConfigHandler();
        }
        return INSTANCE;
    }

    /**
     * 요청에 따라 CORS 설정을 한다.
     *
     * @param handlerInstance 핸들러 인스턴스
     * @param request 요청
     * @param response 응답
     * */
    public void configureCors(Object handlerInstance, Request request, Response response) {
        Class<?> handlerClass = handlerInstance.getClass();
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
}
