package org.sam.server;

import org.sam.server.constant.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class Request {

    private String path;
    private HttpMethod method;
    private Map<String, Object> headers;
    private Map<String, Object> parameterMap;

    private Request(String path, HttpMethod method, Map<String, Object> headers, Map<String, Object> parameterMap) {
        this.path = path;
        this.method = method;
        this.headers = headers;
        this.parameterMap = parameterMap;
    }

    public static Request create(BufferedReader br) {
        UrlParser urlParser = new UrlParser(br);

        Map<String, Object> headers = urlParser.getHeaders();
        HttpMethod method = urlParser.getMethod();
        String path = urlParser.getPath();
        Map<String, Object> parameters = urlParser.getParameters();

        return new Request(path, method, headers, parameters);
    }

    public String getPath() {
        return this.path;
    }

    public HttpMethod getMethod() {
        return this.method;
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

    private static class UrlParser {

        private final BufferedReader br;

        private String path;
        private String method;
        private String parameters;

        public UrlParser(BufferedReader br) {
            this.br = br;
            this.execute();
        }

        private void execute() {
            try {
                String input = br.readLine();
                StringTokenizer parse = new StringTokenizer(input);
                this.method = parse.nextToken().toUpperCase();
                String requestPath = parse.nextToken().toLowerCase();

                this.path = requestPath;
                int index = path.indexOf("?");
                if (index != -1) {
                    this.path = requestPath.substring(0, index);
                    this.parameters = requestPath.substring(index + 1);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String getPath() {
            return this.path;
        }

        public HttpMethod getMethod() {
            return HttpMethod.get(method);
        }

        public Map<String, Object> getParameters() {
            Map<String, Object> map = new HashMap<>();
            if (parameters == null) return map;

            String[] rawParameters = this.parameters.split("&");
            Arrays.stream(rawParameters).forEach(parameter -> {
                String[] parameterPair = parameter.split("=");
                map.put(parameterPair[0], parameterPair[1]);
            });

            return map;
        }

        public Map<String, Object> getHeaders() {
            Map<String, Object> map = new HashMap<>();
            try {
                String s = br.readLine();
                while (!s.trim().equals("")) {
                    int index = s.indexOf(": ");
                    String key = s.substring(0, index);
                    String value = s.substring(index);
                    map.put(key, value);
                    s = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return map;
        }
    }

}
