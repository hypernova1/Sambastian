package org.sam.server;

import org.sam.server.constant.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Request {

    private String path;
    private String method;
    private Map<String, Object> headers;
    private Map<String, Object> parameterMap;

    private Request(String path, String method, Map<String, Object> headers, Map<String, Object> parameterMap) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.parameterMap = parameterMap;
    }

    public static Request create(BufferedReader br) {
        try {
            String input = br.readLine();

            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase();
            String requestPath = parse.nextToken().toLowerCase();

            Map<String, Object> headers = getHeaders(br);

            String path = requestPath;
            int index = path.indexOf("?");
            String parameters;
            Map<String, Object> parameterMap = null;
            if (index != -1) {
                path = requestPath.substring(0, index);
                parameters = requestPath.substring(index + 1);
                parameterMap = getParameterMap(parameters);
            }

            return new Request(path, method, headers, parameterMap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static Map<String, Object> getHeaders(BufferedReader in) throws IOException {
        Map<String, Object> map = new HashMap<>();
        String s = in.readLine();
        while (!s.trim().equals("")) {
            int index = s.indexOf(": ");
            String key = s.substring(0, index);
            String value = s.substring(index);
            map.put(key, value);
            s = in.readLine();
        }
        return map;
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

    public String getPath() {
        return this.path;
    }

    public HttpMethod getMethod() {
        return HttpMethod.get(method);
    }

    public Object getParameter(String key) {
        return this.parameterMap.get(key);
    }

    public Set<String> getParameterNames() {
        return this.parameterMap.keySet();
    }

    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

}
