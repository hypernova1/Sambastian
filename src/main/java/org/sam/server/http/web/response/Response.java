package org.sam.server.http.web.response;

import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.http.Cookie;

import java.util.Set;

/**
 *  응답에 대한 가능을 정의 해놓은 인터페이스.
 *
 * @author hypernova1
 * @see HttpResponse
 * */
public interface Response {

    String DEFAULT_FILE_PAGE = "static/index.html";
    String BAD_REQUEST_PAGE = "static/400.html";
    String NOT_FOUND_PAGE = "static/404.html";
    String FAVICON = "favicon.ico";
    String METHOD_NOT_ALLOWED_PAGE = "static/method_not_allowed.html";
    String BUFFER_SIZE_PROPERTY = ServerProperties.get("file-buffer-size");

    /**
     * HTTP 응답 메시지를 만든다.
     *
     * @param pathOrJson 파일 경로 or JSON
     * @param status 응답 HttpStatus
     * */
    void execute(String pathOrJson, HttpStatus status);

    /**
     * 파비콘에 대한 요청을 처리 한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void favicon();

    /**
     * 루트 경로의 요청을 처리한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void indexFile();

    /**
     * 정적 자원에 대한 처리를 한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void staticResources();

    /**
     * 찾는 정적 자원이 존재하지 않을시 처리한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void notFound();

    /**
     * 잘못된 요청에 대한 처리를 한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void badRequest();

    /**
     * 요청한 URL이 일치하는 핸들러는 있지만 HTTP Method가 일치하지 않을 때에 대한 처리를 한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void methodNotAllowed();

    /**
     * 지원하는 method들을  응답한다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void allowedMethods();

    /**
     * 헤더 정보를 추가한다.
     *
     * @param key 헤더명
     * @param value 헤더값
     * @see org.sam.server.constant.HttpHeader
     * */
    void setHeader(String key, String value);

    /**
     * 응답할 미디어 타입을 설정한다.
     *
     * @param contentMimeType 미디어 타입
     * @see org.sam.server.constant.ContentType
     * */
    void setContentMimeType(ContentType contentMimeType);

    /**
     * OPTION Method으로 요청이 왔을 시 해당 URL로 사용할 수 있는 HttpMethod를 추가한다.
     *
     * @param httpMethod 추가할 HTTP Method
     * @see org.sam.server.constant.HttpMethod
     * */
    void addAllowedMethod(HttpMethod httpMethod);

    /**
     * 쿠키 정보를 추가한다.
     *
     * @param cookie 추가할 쿠키
     * @see org.sam.server.http.Cookie
     * */
    void addCookies(Cookie cookie);

    /**
     * 헤더 정보를 반환한다.
     *
     * @param key 헤더명
     * @return 헤더
     * @see org.sam.server.constant.HttpHeader
     * */
    Object getHeader(String key);

    /**
     * 모든 헤더의 이름을 반환한다.
     *
     * @return 헤더 이름 리스트
     * @see org.sam.server.constant.HttpHeader
     * */
    Set<String> getHeaderNames();

}
