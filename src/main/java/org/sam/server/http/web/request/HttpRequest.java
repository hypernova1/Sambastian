package org.sam.server.http.web.request;

import org.sam.server.constant.HttpMethod;
import org.sam.server.http.Cookie;
import org.sam.server.http.Session;
import org.sam.server.http.SessionManager;

import java.io.InputStream;
import java.util.*;

/**
 * Request 인터페이스의 구현체. 일반적인 HTTP 요청에 대한 정보를 저장한다.
 *
 * @author hypernova1
 * @see Request
 */
public class HttpRequest implements Request {

    private final String protocol;
    private final String path;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Map<String, String> parameterMap;
    private final String json;
    private final Set<Cookie> cookies;

    protected HttpRequest(RequestParser requestParser) {
        this.protocol = requestParser.protocol;
        this.path = requestParser.url;
        this.method = requestParser.httpMethod;
        this.headers = requestParser.headers;
        this.parameterMap = requestParser.parameters;
        this.json = requestParser.json;
        this.cookies = requestParser.cookies;
    }

    /**
     * Http 요청을 분석하여 Request 인스턴스를 반환한다.
     *
     * @param in HTTP 요청을 담은 InputStream
     * @return Request 인스턴스
     */
    public static Request from(InputStream in) {
        RequestParser requestParser = new RequestParser();
        requestParser.parse(in);
        return requestParser.createRequest();
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public String getUrl() {
        return this.path;
    }

    @Override
    public HttpMethod getMethod() {
        return this.method;
    }

    @Override
    public String getParameter(String key) {
        return this.parameterMap.get(key);
    }

    @Override
    public Map<String, String> getParameters() {
        return this.parameterMap;
    }

    @Override
    public Set<String> getParameterNames() {
        return this.parameterMap.keySet();
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    @Override
    public String getHeader(String key) {
        return headers.get(key);
    }

    @Override
    public String getJson() {
        return json;
    }

    @Override
    public Set<Cookie> getCookies() {
        return this.cookies;
    }

    @Override
    public Session getSession() {
        Set<Cookie> cookies = this.getCookies();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (!cookie.getName().equals("sessionId")) continue;

            Session session = SessionManager.getSession(cookie.getValue());
            if (session != null) {
                session.renewAccessTime();
                return session;
            }
            iterator.remove();
        }
        return new Session();
    }

    @Override
    public boolean isFaviconRequest() {
        return this.getUrl().equals("/favicon.ico");
    }

    @Override
    public boolean isResourceRequest() {
        return this.getUrl().startsWith("/resources");
    }

    @Override
    public boolean isRootRequest() {
        return this.getUrl().equals("/") && this.getMethod().equals(HttpMethod.GET);
    }

    @Override
    public boolean isOptionsRequest() {
        return this.getMethod().equals(HttpMethod.OPTIONS);
    }

}
