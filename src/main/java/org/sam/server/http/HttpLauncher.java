package org.sam.server.http;

import org.sam.server.context.HandlerInfo;
import org.sam.server.exception.HandlerNotFoundException;

import java.io.IOException;
import java.net.Socket;

/**
 * Request, Response 인스턴스를 만들고 HTTP 요청을 분기합니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.Request
 * @see org.sam.server.http.Response
 */
public class HttpLauncher {

    /**
     * 소켓을 받아 Request, Response 인스턴스를 만든 후 핸들러 혹은 정적 자원을 찾습니다.
     * 
     * @param connect 소켓
     * */
    static void execute(Socket connect) {
        try {
            HttpRequest httpRequest = Request.create(connect.getInputStream());
            if (httpRequest == null) return;
            HttpResponse httpResponse = HttpResponse.create(connect.getOutputStream(), httpRequest.getPath(), httpRequest.getMethod());
            findHandler(httpRequest, httpResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 요청 URL을 읽어 핸들러를 찾을지 정적 자원을 찾을지 분기합니다.
     * 
     * @param httpRequest 요청 인스턴스
     * @param httpResponse 응답 인스턴스
     * @throws IOException 핸들러를 찾지 못 했을 시
     * */
    private static void findHandler(HttpRequest httpRequest, HttpResponse httpResponse) throws IOException {
        if (isFaviconRequest(httpRequest)) {
            httpResponse.responseFavicon();
            return;
        }
        if (isResourceRequest(httpRequest)) {
            httpResponse.responseStaticResources();
            return;
        }

        try {
            HandlerInfo handlerInfo = HandlerFinder.of(httpRequest, httpResponse).createHandlerInfo();
            if (handlerInfo == null) return;
            HandlerExecutor.of(httpRequest, httpResponse, handlerInfo).execute();
        } catch (HandlerNotFoundException e) {
            httpResponse.notFound();
            throw new IOException(e);
        }
    }

    private static boolean isFaviconRequest(HttpRequest httpRequest) {
        return httpRequest.getPath().equals("/favicon.ico");
    }

    private static boolean isResourceRequest(HttpRequest httpRequest) {
        return httpRequest.getPath().startsWith("/resources");
    }
}
