package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.util.Map;
import java.util.Set;

/**
 * Request 인터페이스의 구현체입니다. 일반적인 HTTP 요청에 대한 정보를 저장합니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.Request
 */
public class HttpRequest implements Request {

    private final String protocol;

    private final String path;

    private final HttpMethod method;

    private final Map<String, String> headers;

    private final Map<String, String> parameterMap;

    private final String json;

    private final Set<Cookie> cookies;

    protected HttpRequest(
            String protocol, String path, HttpMethod method, Map<String, String> headers, Map<String, String> parameterMap,
            String json, Set<Cookie> cookies) {
        this.protocol = protocol;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.parameterMap = parameterMap;
        this.json = json;
        this.cookies = cookies;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public String getPath() {
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
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals("sessionId")) {
                return HttpServer.SessionManager.getSession(cookie.getValue());
            }
        }
        return new Session();
    }

}
