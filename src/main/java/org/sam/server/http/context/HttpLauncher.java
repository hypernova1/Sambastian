package org.sam.server.http.context;

import org.sam.server.context.BeanContainer;
import org.sam.server.context.Handler;
import org.sam.server.context.HandlerNotFoundException;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

/**
 * Request, Response 인스턴스를 만들고 HTTP 요청을 분기한다.
 *
 * @author hypernova1
 * @see Request
 * @see Response
 */
public class HttpLauncher {

    /**
     * 소켓을 받아 Request, Response 인스턴스를 만든 후 핸들러 혹은 정적 자원을 찾는다.
     *
     * @param connect 소켓
     */
    public static void execute(Request request, Response response, BeanContainer beanContainer) {
        //TODO: 핸들러 실행 후에 Response 생성하도록 수정
        try {
            HandlerFinder handlerFinder = HandlerFinder.of(request, response, beanContainer.getHandlerBeans());
            Handler handlerInfo = handlerFinder.find();
            HandlerExecutor handlerExecutor = HandlerExecutor.of(request, response, beanContainer);
            handlerExecutor.execute(handlerInfo);
        } catch (HandlerNotFoundException e) {
            response.notFound();
        }
    }

}
