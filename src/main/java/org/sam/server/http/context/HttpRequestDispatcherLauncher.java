package org.sam.server.http.context;

import org.sam.server.context.Handler;
import org.sam.server.context.HandlerNotFoundException;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;

/**
 * 최선에서 요청을 받아 응답하는 클래스
 *
 * @author hypernova1
 * @see Request
 * @see Response
 */
public class HttpRequestDispatcherLauncher {

    private static HttpRequestDispatcherLauncher INSTANCE;

    private final HandlerExecutor handlerExecutor = HandlerExecutor.getInstance();

    private final HandlerFinder handlerFinder = HandlerFinder.getInstance();

    private HttpRequestDispatcherLauncher() {}

    public static HttpRequestDispatcherLauncher getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new HttpRequestDispatcherLauncher();
        }
        return INSTANCE;
    }

    /**
     * 핸들러를 찾아서 실행한다.
     *
     * @param request 요청
     * @param response 응답
     */
    public void execute(Request request, Response response) {
        try {
            Handler handlerInfo = handlerFinder.find(request, response);
            handlerExecutor.execute(handlerInfo, request, response);
        } catch (HandlerNotFoundException e) {
            response.notFound();
        }
    }

}
