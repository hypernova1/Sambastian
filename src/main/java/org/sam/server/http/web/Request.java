package org.sam.server.http.web;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.http.Cookie;
import org.sam.server.http.CookieStore;
import org.sam.server.http.Session;
import org.sam.server.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * HTTP 요청에 대한 정보를 가지는 클래스입니다.
 *
 * @author hypernova1
 * @see HttpRequest
 * @see HttpMultipartRequest
 */
public interface Request {

    /**
     * 프로토콜을 반환합니다.
     * 
     * @return HTTP 프로토콜
     * */
    String getProtocol();

    /**
     * 요청 URL을 반환합니다.
     *
     * @return 요청 URL
     * */
    String getUrl();

    /**
     * HTTP Method를 반환합니다.
     *
     * @return Http Method
     * */
    HttpMethod getMethod();

    /**
     * 이름에 해당하는 파라미터의 값을 반환합니다.
     *
     * @param key 파라미터 이름
     * @return 파라미터 값
     * */
    String getParameter(String key);

    /**
     * 모든 파라미터를 반환합니다.
     * 
     * @return 모든 파라미터 목록
     * */
    Map<String, String> getParameters();

    /**
     * 모든 파라미터의 이름을 반환합니다.
     * 
     * @return 모든 파라미터의 이름
     * */
    Set<String> getParameterNames();

    /**
     * 모든 헤더의 이름을 반환합니다.
     * 
     * @return 모든 헤더의 이름
     * */
    Set<String> getHeaderNames();

    /**
     * 이름에 해당하는 헤더 값을 반환합니다.
     * 
     * @param key 헤더 이름
     * @return 헤더 값
     * */
    String getHeader(String key);

    /**
     * JSON을 반환합니다.
     * 
     * @return JSON
     * */
    String getJson();

    /**
     * 쿠키 목록을 반환합니다.
     * 
     * @return 쿠키 목록
     * */
    Set<Cookie> getCookies();

    /**
     * 세션을 반환합니다.
     * 
     * @return 세션
     * */
    Session getSession();

}
