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

        Request.UrlParser urlParser = new Request.UrlParser(in);

        Map<String, String> headers = urlParser.headers;
        HttpMethod method = urlParser.method;
        String path = urlParser.path;
        Map<String, String> parameters = urlParser.parameters;
        Map<String, String> attributes = urlParser.attributes;
        String json = urlParser.json;
        List<Cookie> cookies = urlParser.cookies;

        System.out.println(headers.get("Content-Type"));
//        if (ContentType.valueOf(headers.get("Content-Type")).equals(ContentType.MULTIPART_FORM_DATA)) {
//            return new HttpMultipartRequest(path, method, headers, parameters, attributes, json, cookies, null);
//        }
        return new HttpRequest(path, method, headers, parameters, attributes, json, cookies);
    }

    class UrlParser {
        protected String path;
        protected HttpMethod method;
        protected Map<String, String> headers = new HashMap<>();
        protected Map<String, String> parameters = new HashMap<>();
        protected Map<String, String> attributes = new HashMap<>();
        protected String json;
        protected List<Cookie> cookies = new ArrayList<>();

        public UrlParser(InputStream in) {
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

                String contentType = headers.get("content-type");
                if (HttpMethod.get(method).equals(HttpMethod.POST) ||
                        HttpMethod.get(method).equals(HttpMethod.PUT) ||
                        ContentType.APPLICATION_JSON.getValue().equals(contentType)) {
                    String requestBody;

                    while ((requestBody = br.readLine()) != null) {
                        System.out.println(requestBody);
                    }
                    this.attributes = parseQuery(requestBody);
                    System.out.println(requestBody);
                    if (ContentType.APPLICATION_JSON.getValue().equals(contentType) && this.attributes == null) {
                        this.json = requestBody;
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
            if (parameters.startsWith("{") && parameters.endsWith("}")) return null;

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
    }
}
