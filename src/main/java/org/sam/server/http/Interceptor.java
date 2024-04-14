package org.sam.server.http;

import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

/**
 * 핸들러 전/후로 실행되는 메서드를 가진 인터페이스입니다.
 * 상속 받아 구현하면 핸들러 실행 전/후로 호출 됩니다.
 *
 * @author hypernova1
 * */
public interface Interceptor {

    /**
     * 핸들러 호출 전 실행됩니다.
     * 
     * @param request 요청 인스턴스
     * @param response 응답 인스턴스
     * */
    void preHandler(Request request, Response response);

    /**
     * 핸들러 호출 후 실행됩니다.
     *
     * @param request 요청 인스턴스
     * @param response 응답 인스턴스
     * */
    void postHandler(Request request, Response response);

}
