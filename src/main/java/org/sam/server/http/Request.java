package org.sam.server.http;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;

import java.io.*;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 5:19 PM
 */
public interface Request {

    static HttpRequest create(InputStream in) {
        return new Request.UrlParser(in).createRequest();
    }

    String getPath();

    HttpMethod getMethod();

    String getParameter(String key);

    Map<String, String> getParameters();

    Set<String> getParameterNames();

    Set<String> getHeaderNames();

    String getHeader(String key);

    Map<String, Object> getAttributes();

    String getJson();

    List<Cookie> getCookies();

    class UrlParser {
        protected String path;
        protected HttpMethod method;
        protected Map<String, String> headers = new HashMap<>();
        protected Map<String, String> parameters = new HashMap<>();
        protected Map<String, Object> attributes = new HashMap<>();
        protected String json;
        protected List<Cookie> cookies = new ArrayList<>();

        public UrlParser(InputStream in) {
            parse(in);
        }

        private void parse(InputStream in) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(in));

                String input = br.readLine();
                StringTokenizer parse = new StringTokenizer(input);
                String method = parse.nextToken().toUpperCase();
                String requestPath = parse.nextToken().toLowerCase();
                String query = parsePathAndGetQuery(requestPath);

                if (!query.isEmpty()) {
                    this.parameters = parseQuery(query);
                }

                parseHeaders(br);
                parseMethod(method);

                String contentType = headers.get("content-type") != null ? headers.get("content-type") : "";
                String boundary = null;
                if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue())) {
                    boundary = "--" + contentType.split("; ")[1].split("=")[1];
                }

                if (HttpMethod.get(method).equals(HttpMethod.POST) ||
                        HttpMethod.get(method).equals(HttpMethod.PUT) ||
                        ContentType.APPLICATION_JSON.getValue().equals(contentType)) {

                    String temp;
                    StringBuilder requestBody = new StringBuilder();
                    while ((temp = br.readLine()) != null) {
                        System.out.println(temp);
                        requestBody.append(temp).append("\n");
                    }
                    if (ContentType.APPLICATION_JSON.getValue().equals(contentType) && this.attributes == null) {
                        this.json = requestBody.toString();
                    }
                    if (boundary != null) {
                        this.attributes = parseMultipartBody(requestBody.toString(), boundary);
                    } else {
                        this.attributes = parseRequestBody(requestBody.toString());
                    }
                }
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
                    String value = s.substring(index + 2);
                    if ("cookie".equals(key)) {
                        CookieStore cookieStore = new CookieStore();
                        this.cookies = cookieStore.parseCookie(value);
                        s = br.readLine();
                        continue;
                    }

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

        private String parsePathAndGetQuery(String requestPath) {
            int index = requestPath.indexOf("?");
            if (index != -1) {
                this.path = requestPath.substring(0, index);
                return requestPath.substring(index + 1);
            }
            this.path = requestPath;
            return "";
        }

        private Map<String, String> parseQuery(String parameters) {
            Map<String, String> map = new HashMap<>();
            String[] rawParameters = parameters.split("&");
            Arrays.stream(rawParameters).forEach(parameter -> {
                String[] parameterPair = parameter.split("=");
                String name = parameterPair[0];
                String value = null;
                if (parameterPair.length == 2) {
                    value = parameterPair[1];
                }
                map.put(name, value);
            });
            return map;
        }

        private Map<String, Object> parseRequestBody(String requestBody) {
            if (requestBody.startsWith("{") && requestBody.endsWith("}")) return null;
            Map<String, Object> map = new HashMap<>();
            String[] rawParameters = requestBody.split("&");
            Arrays.stream(rawParameters).forEach(parameter -> {
                String[] parameterPair = parameter.split("=");
                String name = parameterPair[0];
                String value = null;
                if (parameterPair.length == 2) {
                    value = parameterPair[1];
                }
                map.put(name, value);
            });
            return map;
        }

        private Map<String, Object> parseMultipartBody(String requestBody, String boundary) {

            String[] elements = requestBody.split(boundary);
            for (int i = 1; i < elements.length; i++) {
//                System.out.print(elements[i]);
                String[] nodes = elements[i].split("; ");
                if (nodes.length == 1) {

                }
            }
            return null;
        }

        public HttpRequest createRequest() {
            Map<String, String> headers = this.headers;
            HttpMethod method = this.method;
            String path = this.path;
            Map<String, String> parameters = this.parameters;
            Map<String, Object> attributes = this.attributes;
            String json = this.json;
            List<Cookie> cookies = this.cookies;

            String contentType = headers.get("content-type") != null ? headers.get("content-type") : "";
            if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue())) {
                return new HttpMultipartRequest(path, method, headers, parameters, attributes, json, cookies, null);
            }
            return new HttpRequest(path, method, headers, parameters, attributes, json, cookies);
        }
    }
}
