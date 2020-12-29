package org.sam.server.http;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;

import java.util.Set;

/**
 *  응답에 대한 가능을 정의 해놓은 인터페이스입니다.
 *
 * @author hypernova1
 * @see HttpResponse
 * */
public interface Response {

    /**
     * HTTP 응답 메시지를 만듭니다.
     *
     * @param pathOrJson 파일 경로 or JSON
     * @param status 응답 HttpStatus
     * */
    void execute(String pathOrJson, HttpStatus status);

    /**
     * 파비콘에 대한 요청을 처리 합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void favicon();

    /**
     * 루트 경로의 요청을 처리합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void indexFile();

    /**
     * 정적 자원에 대한 처리를 합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void staticResources();

    /**
     * 찾는 정적 자원이 존재하지 않을시 처리합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void notFound();

    /**
     * 잘못된 요청에 대한 처리를 합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void badRequest();

    /**
     * 요청한 URL이 일치하는 핸들러는 있지만 HTTP Method가 일치하지 않을 때에 대한 처리를 합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void methodNotAllowed();

    /**
     * 지원하는 method들을  응답합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    void allowedMethods();

    /**
     * 헤더 정보를 추가합니다.
     *
     * @param key 헤더명
     * @param value 헤더값
     * @see org.sam.server.constant.HttpHeader
     * */
    void setHeader(String key, String value);

    /**
     * 응답할 미디어 타입을 설정합니다.
     *
     * @param contentMimeType 미디어 타입
     * @see org.sam.server.constant.ContentType
     * */
    void setContentMimeType(ContentType contentMimeType);

    /**
     * OPTION Method으로 요청이 왔을 시 해당 URL로 사용할 수 있는 HttpMethod를 추가합니다.
     *
     * @param httpMethod 추가할 HTTP Method
     * @see org.sam.server.constant.HttpMethod
     * */
    void addAllowedMethod(HttpMethod httpMethod);

    /**
     * 쿠키 정보를 추가합니다.
     *
     * @param cookie 추가할 쿠키
     * @see org.sam.server.http.Cookie
     * */
    public void addCookies(Cookie cookie);

    /**
     * 헤더 정보를 반환합니다.
     *
     * @param key 헤더명
     * @return 헤더
     * @see org.sam.server.constant.HttpHeader
     * */
    public Object getHeader(String key);

    /**
     * 모든 헤더의 이름을 반환합니다.
     *
     * @return 헤더 이름 리스트
     * @see org.sam.server.constant.HttpHeader
     * */
    public Set<String> getHeaderNames();

}
