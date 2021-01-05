package org.sam.server.http.context;

import org.sam.server.constant.HttpMethod;
import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.web.HttpResponse;
import org.sam.server.http.web.Request;
import org.sam.server.http.web.Response;

import java.io.IOException;
import java.net.Socket;

/**
 * Request, Response 인스턴스를 만들고 HTTP 요청을 분기합니다.
 *
 * @author hypernova1
 * @see Request
 * @see Response
 */
public class HttpLauncher {

    /**
     * 소켓을 받아 Request, Response 인스턴스를 만든 후 핸들러 혹은 정적 자원을 찾습니다.
     * 
     * @param connect 소켓
     * */
    public static void execute(Socket connect) {
        try {
            Request request = Request.of(connect.getInputStream());
            if (isEmptyRequest(request)) {
                return;
            }
            Response response = HttpResponse.of(connect.getOutputStream(), request.getPath(), request.getMethod());
            findHandler(request, response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 요청 URL을 읽어 핸들러를 찾을지 정적 자원을 찾을지 분기합니다.
     * 
     * @param request 요청 인스턴스
     * @param response 응답 인스턴스
     * */
    private static void findHandler(Request request, Response response) {
        if (isFaviconRequest(request)) {
            response.favicon();
            return;
        }
        if (isResourceRequest(request)) {
            response.staticResources();
            return;
        }

        if (isIndexRequest(request)) {
            response.indexFile();
            return;
        }

        if (isOptionsRequest(request)) {
            response.allowedMethods();
            return;
        }

        try {
            HandlerFinder handlerFinder = HandlerFinder.create(request, response);
            HandlerInfo handlerInfo = handlerFinder.createHandlerInfo();
            HandlerExecutor handlerExecutor = HandlerExecutor.create(request, response, handlerInfo);
            handlerExecutor.execute();
        } catch (HandlerNotFoundException e) {
            response.notFound();
        }
    }

    private static boolean isIndexRequest(Request request) {
        return request.getPath().equals("/") && request.getMethod().equals(HttpMethod.GET);
    }

    private static boolean isFaviconRequest(Request request) {
        return request.getPath().equals("/favicon.ico");
    }

    private static boolean isResourceRequest(Request request) {
        return request.getPath().startsWith("/resources");
    }

    private static boolean isEmptyRequest(Request request) {
        return request == null;
    }

    private static boolean isOptionsRequest(Request request) {
        return request.getMethod().equals(HttpMethod.OPTIONS);
    }

}
