package org.sam.server.http.handler;

import org.sam.server.bean.BeanContainer;
import org.sam.server.bean.Handler;
import org.sam.server.bean.HandlerNotFoundException;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 실행할 핸들러를 찾는 클래스
 *
 * @author hypernova1
 */
public class HandlerFinder {

    private static HandlerFinder INSTANCE;

    private final List<Object> handlerBeans = BeanContainer.getInstance().getHandlerBeans();

    private HandlerFinder() {}

    public static HandlerFinder getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HandlerFinder();
        }
        return INSTANCE;
    }

    /**
     * 요청에 맞는 핸들러를 찾아 반환한다.
     *
     * @param request 요청
     * @param response 응답
     * @return 핸들러
     * */
    public Handler find(Request request, Response response) throws HandlerNotFoundException {
        for (Object handlerInstance : handlerBeans) {
            Class<?> handlerType = handlerInstance.getClass();
            HandlerClassifier classifier = new HandlerClassifier();
            classifier.classifyHandler(handlerType);

            String handlerClassPath = handlerType.getDeclaredAnnotation(org.sam.server.annotation.component.Handler.class).value();
            HandlerMethodMatcher matcher = new HandlerMethodMatcher(request, response, handlerClassPath);

            Method handlerMethod = matcher.findHandlerMethod(classifier.getHandlerMethods(), classifier.getPathValueHandlerMethods())
                    .orElse(null);

            if (handlerMethod == null) continue;
            if (!handlerInstance.getClass().equals(handlerMethod.getDeclaringClass())) {
                continue;
            }
            return Handler.of(handlerInstance, handlerMethod);
        }

        throw new HandlerNotFoundException();
    }

}
