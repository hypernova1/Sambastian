package org.sam.server;

import java.util.*;

public class Request {

    private String path;
    private String method;
    private Map<String, Object> parameterMap;

    private Request(String path, String method, Map<String, Object> parameterMap) {
        this.path = path;
        this.method = method;
        this.parameterMap = parameterMap;
    }

    public static Request create(String input) {
        StringTokenizer parse = new StringTokenizer(input);

        String method = parse.nextToken().toUpperCase();
        String requestPath = parse.nextToken().toLowerCase();

        String path = requestPath;
        int index = path.indexOf("?");
        String parameters;
        Map<String, Object> parameterMap = null;
        if (index != -1) {
            path = requestPath.substring(0, index);
            parameters = requestPath.substring(index);
            parameterMap = getParameterMap(parameters);
        }

        return new Request(path, method, parameterMap);
    }

    private static Map<String, Object> getParameterMap(String parameters) {
        String[] rawParameters = parameters.split("&");
        Map<String, Object> map = new HashMap<>();
        Arrays.stream(rawParameters).forEach(parameter -> {
            String[] parameterPair = parameter.split("=");
            map.put(parameterPair[0], parameterPair[1]);
        });

        return map;
    }

    public String getContentMimeType() {
        if (this.path.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    public String getPath() {
        return this.path;
    }

    public String getMethod() {
        return this.method;
    }

    public Object getParameter(String key) {
        return this.parameterMap.get(key);
    }

    public Set<String> getParameterNames() {
        return this.parameterMap.keySet();
    }

}
