package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpRequest implements Request {

    private String protocol;
    private final String path;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Map<String, String> parameterMap;
    private final Map<String, String> attributes;
    private final String json;
    private final Set<Cookie> cookies;

    protected HttpRequest(
            String protocol, String path, HttpMethod method, Map<String, String> headers, Map<String, String> parameterMap,
            Map<String, String> attributes, String json, Set<Cookie> cookies) {
        this.protocol = protocol;
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.parameterMap = parameterMap;
        this.attributes = attributes;
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
    public Map<String, String> getAttributes() {
        return attributes;
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
