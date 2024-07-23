package org.sam.server.http.handler;

import org.sam.server.bean.BeanContainer;
import org.sam.server.http.Interceptor;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

import java.util.List;

/**
 * 인터셉터 실행 클래스
 * */
public class InterceptorExecutor {

    private static InterceptorExecutor INSTANCE;

    private final BeanContainer beanContainer = BeanContainer.getInstance();

    private InterceptorExecutor() {}

    public static InterceptorExecutor getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new InterceptorExecutor();
        }
        return INSTANCE;
    }

    /**
     * 선언된 인터셉터의 preHandler 함수를 실행한다.
     *
     * @param request 요청
     * @param response 응답
     * */
    public void executePreHandlers(Request request, Response response) {
        List<Interceptor> interceptors = beanContainer.getInterceptors();

        for (Interceptor interceptor : interceptors) {
            interceptor.preHandler(request, response);
        }
    }

    /**
     * 선언된 인터셉터의 postHandler 함수를 실행한다.
     *
     * @param request 요청
     * @param response 응답
     * */
    public void executePostHandlers(Request request, Response response) {
        List<Interceptor> interceptors = beanContainer.getInterceptors();

        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).postHandler(request, response);
        }
    }
}
