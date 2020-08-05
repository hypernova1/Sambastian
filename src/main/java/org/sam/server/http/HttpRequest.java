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

    private final String path;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final Map<String, String> parameterMap;
    private final Map<String, Object> attributes;
    private final String json;
    private final Set<Cookie> cookies;

    protected HttpRequest(
            String path, HttpMethod method, Map<String, String> headers, Map<String, String> parameterMap,
            Map<String, Object> attributes, String json, Set<Cookie> cookies) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.parameterMap = parameterMap;
        this.attributes = attributes;
        this.json = json;
        this.cookies = cookies;
    }

    public String getPath() {
        return this.path;
    }

    public HttpMethod getMethod() {
        return this.method;
    }

    public String getParameter(String key) {
        return this.parameterMap.get(key);
    }

    public Map<String, String> getParameters() {
        return this.parameterMap;
    }

    public Set<String> getParameterNames() {
        return this.parameterMap.keySet();
    }

    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    public String getHeader(String key) {
        return headers.get(key);
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public String getJson() {
        return json;
    }

    public Set<Cookie> getCookies() {
        return this.cookies;
    }

}
