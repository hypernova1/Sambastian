package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
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
        private String path;
        private HttpMethod method;
        private Map<String, Object> headers = new HashMap<>();
        private Map<String, Object> parameters = new HashMap<>();

        public UrlParser(BufferedReader br) {
            try {
                String input = br.readLine();
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase();
                String requestPath = parse.nextToken().toLowerCase();

                String rawParameters = parsePath(requestPath);

                if (rawParameters != null) {
                    parseParameters(rawParameters);
                }

                parseHeaders(br);
                parseMethod(method);
            } catch (IOException e) {
                System.out.println("terminate thread..");
                e.printStackTrace();
            }
        }

        private void parseHeaders(BufferedReader br) {
            try {
                String s = br.readLine();
                while (!s.trim().equals("")) {
                    int index = s.indexOf(": ");
                    String key = s.substring(0, index).toLowerCase();
                    String value = s.substring(index);
                    this.headers.put(key, value);
                    s = br.readLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void parseMethod(String method) {
            this.method = HttpMethod.get(method);
        }

        private String parsePath(String requestPath) {
            this.path = requestPath;
            int index = path.indexOf("?");
            if (index != -1) {
                this.path = requestPath.substring(0, index);
                return requestPath.substring(index + 1);
            }
            return null;
        }

        private void parseParameters(String parameters) {
            String[] rawParameters = parameters.split("&");
            Arrays.stream(rawParameters).forEach(parameter -> {
                String[] parameterPair = parameter.split("=");
                this.parameters.put(parameterPair[0], parameterPair[1]);
            });
        }

        public Map<String, Object> getHeaders() {
            return this.headers;
        }

        public HttpMethod getMethod() {
            return this.method;
        }

        public String getPath() {
            return this.path;
        }

        public Map<String, Object> getParameters() {
            return this.parameters;
        }
    }
}
