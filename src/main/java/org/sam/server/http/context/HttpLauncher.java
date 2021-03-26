package org.sam.server.http.context;

import org.sam.server.constant.HttpMethod;
import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;
import org.sam.server.http.web.HttpRequest;
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
            Request request = HttpRequest.from(connect.getInputStream());
            if (request == null) {
                return;
            }
            Response response = HttpResponse.of(connect.getOutputStream(), request.getUrl(), request.getMethod());
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
            HandlerFinder handlerFinder = HandlerFinder.of(request, response);
            HandlerInfo handlerInfo = handlerFinder.createHandlerInfo();
            HandlerExecutor handlerExecutor = HandlerExecutor.of(request, response, handlerInfo);
            handlerExecutor.execute();
        } catch (HandlerNotFoundException e) {
            response.notFound();
        }
    }

    /**
     * 인덱스 페이지 요청인지에 대한 여부를 반환한다.
     *
     * @param request 요청 정보
     * @return 인덱스 페이지 여부
     * */
    private static boolean isIndexRequest(Request request) {
        return request.getUrl().equals("/") && request.getMethod().equals(HttpMethod.GET);
    }

    /**
     * 파비콘 요청인지에 대한 여부를 반환한다.
     *
     * @param request 요청 정보
     * @return 파비콘 요청 여부
     * */
    private static boolean isFaviconRequest(Request request) {
        return request.getUrl().equals("/favicon.ico");
    }

    /**
     * 정적 자원 요청인지에 대한 여부를 반환한다.
     *
     * @param request 요청 정보
     * @return 정적 자원 요청 여부
     * */
    private static boolean isResourceRequest(Request request) {
        return request.getUrl().startsWith("/resources");
    }

    /**
     * OPTION 요청인지에 대한 여부를 반환한다.
     *
     * @param request 요청 정보
     * @return OPTION 요청 여부
     * */
    private static boolean isOptionsRequest(Request request) {
        return request.getMethod().equals(HttpMethod.OPTIONS);
    }

}
