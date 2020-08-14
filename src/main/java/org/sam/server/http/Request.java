package org.sam.server.http;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 5:19 PM
 */
@SuppressWarnings("unused")
public interface Request {

    static HttpRequest create(InputStream in) {
        return new UrlParser(in).createRequest();
    }

    String getProtocol();

    String getPath();

    HttpMethod getMethod();

    String getParameter(String key);

    Map<String, String> getParameters();

    Set<String> getParameterNames();

    Set<String> getHeaderNames();

    String getHeader(String key);

    Map<String, String> getAttributes();

    String getJson();

    Set<Cookie> getCookies();

    Session getSession();

    class UrlParser {
        private static final Logger logger = LoggerFactory.getLogger(Request.class);

        protected String protocol;
        protected String path;
        protected HttpMethod method;
        protected Map<String, String> headers = new HashMap<>();
        protected Map<String, String> parameters = new HashMap<>();
        protected Map<String, String> attributes = new HashMap<>();
        protected String json;
        protected Set<Cookie> cookies = new HashSet<>();
        protected Map<String, Object> files = new HashMap<>();

        private UrlParser(InputStream in) {
            parse(in);
        }

        private void parse(InputStream in) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(in);
                StringBuilder sb = new StringBuilder();
                String headersPart = "";
                int i;
                while ((i = inputStream.read()) != -1) {
                    char c = (char) i;
                    sb.append(c);
                    if (sb.toString().endsWith("\r\n\r\n")) {
                        headersPart = sb.toString().replace("\r\n\r\n", "");
                        break;
                    }
                }
                if (headersPart.trim().isEmpty()) return;
                String[] headers = headersPart.split("\r\n");
                StringTokenizer parse = new StringTokenizer(headers[0]);
                String method = parse.nextToken().toUpperCase();
                String requestPath = parse.nextToken().toLowerCase();
                this.protocol = parse.nextToken().toUpperCase();
                String query = parseRequestPath(requestPath);
                parseHeaders(headers);
                parseMethod(method);
                if (StringUtils.isNotEmpty(query)) this.parameters = parseQuery(query);
                String contentType = this.headers.get("content-type") != null ? this.headers.get("content-type") : "";
                if (HttpMethod.get(method).equals(HttpMethod.POST) ||
                        HttpMethod.get(method).equals(HttpMethod.PUT) ||
                        ContentType.APPLICATION_JSON.getValue().equals(contentType)) {
                    if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue())) {
                        String boundary = "--" + contentType.split("; ")[1].split("=")[1];
                        parseMultipartBody(inputStream, boundary);
                    } else {
                        parseRequestBody(inputStream, contentType);
                    }
                }
            } catch (IOException e) {
                logger.error("terminate thread..");
                e.printStackTrace();
            }
        }

        private void parseRequestBody(InputStream inputStream, String contentType) throws IOException {
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = inputStream.read()) != -1) {
                char c = (char) i;
                sb.append(c);
            }
            if (ContentType.APPLICATION_JSON.getValue().equals(contentType)
                    && this.attributes == null) {
                this.json = sb.toString();
                return;
            }
            this.attributes = parseQuery(sb.toString());
        }

        private void parseHeaders(String[] headers) {
            for (int i = 1; i < headers.length; i++) {
                int index = headers[i].indexOf(": ");
                String key = headers[i].substring(0, index).toLowerCase();
                String value = headers[i].substring(index + 2);
                if ("cookie".equals(key)) {
                    this.cookies = CookieStore.parseCookie(value);
                    continue;
                }
                this.headers.put(key, value);
            }
        }

        private void parseMethod(String method) {
            this.method = HttpMethod.get(method);
        }

        private String parseRequestPath(String requestPath) {
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
            Arrays.stream(rawParameters).forEach(rawParameter -> {
                String[] parameterPair = rawParameter.split("=");
                String name = parameterPair[0];
                String value = "";
                if (parameterPair.length == 2) {
                    value = parameterPair[1];
                }
                map.put(name, value);
            });
            return map;
        }

        private void parseMultipartBody(InputStream inputStream, String boundary) throws IOException {
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = inputStream.read()) != -1) {
                sb.append((char) i);
                if (sb.toString().contains(boundary + "\r\n")) {
                    parseMultipartLine(inputStream, boundary);
                    return;
                }
            }
        }

        private void parseMultipartLine(InputStream inputStream, String boundary) throws IOException {
            int i = 0;
            int loopCnt = 0;
            String name = "";
            String value = "";
            String filename = "";
            String contentType = "";
            byte[] fileData = null;
            boolean isFile = false;
            int inputStreamLength = inputStream.available();
            byte[] data = new byte[inputStreamLength];
            int binary;
            while ((binary = inputStream.read()) != -1) {
                data[i] = (byte) binary;
                if (i != 0 && data[i - 1] == '\r' && data[i] == '\n') {
                    data = Arrays.copyOfRange(data, 0, i);
                    String line = new String(data, StandardCharsets.UTF_8);
                    data = new byte[inputStreamLength];
                    i = 0;
                    if (loopCnt == 0) {
                        loopCnt++;
                        int index = line.indexOf("\"");
                        if (index == -1) continue;
                        String[] split = line.split("\"");
                        name = split[1];
                        if (split.length == 5) {
                            filename = split[3];
                            isFile = true;
                        }
                        continue;
                    } else if (loopCnt == 1 && isFile) {
                        int index = line.indexOf(": ");
                        contentType = line.substring(index + 2);
                        fileData = parseFile(inputStream, boundary);
                        loopCnt = 0;
                        if (fileData == null) continue;
                        line = boundary;
                    } else if (loopCnt == 1 && !line.contains(boundary)) {
                        value = line;
                        loopCnt = 0;
                        continue;
                    }

                    if (line.contains(boundary)) {
                        if (!filename.equals("")) createMultipartFile(name, filename, contentType, fileData);
                        else this.attributes.put(name, value);

                        name = "";
                        value = "";
                        filename = "";
                        contentType = "";
                        fileData = null;
                        loopCnt = 0;
                    }
                    if (inputStream.available() == 0) return;
                }
                i++;
            }
        }

        private void createMultipartFile(String name, String filename, String contentType, byte[] fileData) {
            MultipartFile multipartFile = new MultipartFile(filename, contentType, fileData);
            if (this.files.get(name) == null) {
                this.files.put(name, multipartFile);
                return;
            }
            Object file = this.files.get(name);
            addMultipartFileToList(name, multipartFile, file);
        }

        @SuppressWarnings("unchecked")
        private void addMultipartFileToList(String name, MultipartFile multipartFile, Object file) {
            if (file.getClass().equals(ArrayList.class)) {
                ((ArrayList<MultipartFile>) file).add(multipartFile);
                return;
            }
            List<MultipartFile> files = new ArrayList<>();
            MultipartFile preFile = (MultipartFile) file;
            files.add(preFile);
            files.add(multipartFile);
            this.files.put(name, files);
        }

        private byte[] parseFile(InputStream inputStream, String boundary) throws IOException {
            int i;
            int fileLength = 0;
            byte[] data = new byte[1024 * 8];
            while ((i = inputStream.read()) != -1) {
                if (data.length == fileLength) {
                    byte[] temp = new byte[data.length * 2];
                    System.arraycopy(data, 0, temp, 0, data.length);
                    data = temp;
                }
                data[fileLength] = (byte) i;
                if (fileLength != 0 && data[fileLength - 1] == '\r' && data[fileLength] == '\n') {
                    String content = new String(data, StandardCharsets.UTF_8);
                    if (content.trim().equals(boundary)) return null;
                    boundary = new String(boundary.getBytes(), StandardCharsets.UTF_8);
                    int index = content.indexOf(boundary);
                    if (index != -1) break;
                }
                fileLength++;
            }
            return Arrays.copyOfRange(data, 2, fileLength - boundary.getBytes().length);
        }

        public HttpRequest createRequest() {
            if (this.headers.isEmpty()) return null;
            Map<String, String> headers = this.headers;
            HttpMethod method = this.method;
            String path = this.path;
            Map<String, String> parameters = this.parameters;
            Map<String, String> attributes = this.attributes;
            String json = this.json;
            Set<Cookie> cookies = this.cookies;
            String contentType = headers.get("content-type") != null ? headers.get("content-type") : "";
            if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue()))
                return new HttpMultipartRequest(protocol, path, method, headers, parameters, attributes, json, cookies, files);
            return new HttpRequest(protocol, path, method, headers, parameters, attributes, json, cookies);
        }
    }
}
