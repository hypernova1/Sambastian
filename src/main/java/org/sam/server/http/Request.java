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
 * HTTP 요청에 대한 정보를 가지는 클래스입니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.HttpRequest
 * @see org.sam.server.http.HttpMultipartRequest
 */
public interface Request {

    static Request of(InputStream in) {
        return new UrlParser(in).createRequest();
    }

    /**
     * HTTP/HTTPS를 반환합니다.
     * 
     * @return HTTP 프로토콜
     * */
    String getProtocol();

    /**
     * 요청 URL을 반환합니다.
     *
     * @return 요청 URL
     * */
    String getPath();

    /**
     * HTTP Method를 반환합니다.
     *
     * @return Http Method
     * */
    HttpMethod getMethod();

    /**
     * 이름에 해당하는 파라미터의 값을 반환합니다.
     *
     * @param key 파라미터 이름
     * @return 파라미터 값
     * */
    String getParameter(String key);

    /**
     * 모든 파라미터를 반환합니다.
     * 
     * @return 모든 파라미터 목록
     * */
    Map<String, String> getParameters();

    /**
     * 모든 파라미터의 이름을 반환합니다.
     * 
     * @return 모든 파라미터의 이름
     * */
    Set<String> getParameterNames();

    /**
     * 모든 헤더의 이름을 반환합니다.
     * 
     * @return 모든 헤더의 이름
     * */
    Set<String> getHeaderNames();

    /**
     * 이름에 해당하는 헤더 값을 반환합니다.
     * 
     * @param key 헤더 이름
     * @return 헤더 값
     * */
    String getHeader(String key);

    /**
     * JSON을 반환합니다.
     * 
     * @return JSON
     * */
    String getJson();

    /**
     * 쿠키 목록을 반환합니다.
     * 
     * @return 쿠키 목록
     * */
    Set<Cookie> getCookies();

    /**
     * 모든 세션을 반환합니다.
     * 
     * @return 세션 목록
     * */
    Session getSession();

    /**
     * 소켓으로 부터 받은 InputStream을 읽어 Request 인스턴스를 생성하는 클래스입니다.
     *
     * @author hypernova1
     * @see org.sam.server.http.Request
     * @see org.sam.server.http.HttpRequest
     * @see org.sam.server.http.HttpMultipartRequest
     * */
    class UrlParser {

        private static final Logger logger = LoggerFactory.getLogger(Request.class);

        protected String protocol;

        protected String path;

        protected HttpMethod method;

        protected Map<String, String> headers = new HashMap<>();

        protected Map<String, String> parameters = new HashMap<>();

        protected String json;

        protected Set<Cookie> cookies = new HashSet<>();

        protected Map<String, Object> files = new HashMap<>();

        private UrlParser(InputStream in) {
            parse(in);
        }

        /**
         * InputStream에서 HTTP 본문을 읽은 후 파싱합니다.
         *
         * @param in 소켓의 InputStream
         * */
        private void parse(InputStream in) {
            try {
                BufferedInputStream inputStream = new BufferedInputStream(in);

                String headersPart = readHeader(inputStream);

                if (isNonHttpRequest(headersPart)) return;

                String[] headers = headersPart.split("\r\n");
                StringTokenizer parse = new StringTokenizer(headers[0]);
                String method = parse.nextToken().toUpperCase();
                String requestPath = parse.nextToken().toLowerCase();
                this.protocol = parse.nextToken().toUpperCase();
                String query = parseRequestPath(requestPath);

                parseHeaders(headers);
                parseMethod(method);

                if (StringUtils.isNotEmpty(query))
                    this.parameters = parseQuery(query);

                String contentType = this.headers.getOrDefault("content-type", "");

                if (existHttpBody(method, contentType)) {
                    parseBodyText(inputStream, contentType);
                }
            } catch (IOException e) {
                logger.error("terminate thread..");
                e.printStackTrace();
            }
        }

        /**
         * HTTP 바디에 있는 데이터를 파싱합니다.
         *
         * @param inputStream 인풋 스트림
         * @param contentType 미디어 타입
         * */
        private void parseBodyText(BufferedInputStream inputStream, String contentType) throws IOException {
            if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue())) {
                String boundary = "--" + contentType.split("; ")[1].split("=")[1];
                parseMultipartBody(inputStream, boundary);
                return;
            }
            parseRequestBody(inputStream, contentType);
        }

        /**
         * HTTP 바디에 메시지가 존재하는 지 확인합니다.
         *
         * @param method HTTP 메서드 타입
         * @param contentType 미디어 타입
         *
         * @return HTTP 바디에 메시지가 존재하는지 여부
         * */
        private boolean existHttpBody(String method, String contentType) {
            return HttpMethod.get(method).equals(HttpMethod.POST) ||
                    HttpMethod.get(method).equals(HttpMethod.PUT) ||
                    ContentType.APPLICATION_JSON.getValue().equals(contentType);
        }

        /**
         * HTTP 헤더를 읽어 반환합니다.
         *
         * @param inputStream 인풋 스트림
         * @return HTTP 헤더 내용
         * */
        private String readHeader(BufferedInputStream inputStream) throws IOException {
            int i;
            String headersPart = "";
            StringBuilder sb = new StringBuilder();
            while ((i = inputStream.read()) != -1) {
                char c = (char) i;
                sb.append(c);
                if (isCompleteHeader(sb.toString())) {
                    headersPart = sb.toString().replace("\r\n\r\n", "");
                    break;
                }
            }
            return headersPart;
        }

        /**
         * HTTP 요청이 아닌지 확인합니다.
         *
         * @param headersPart 헤더
         * @return HTTP 요청이 아닌지에 대한 여부
         * */
        private boolean isNonHttpRequest(String headersPart) {
            return headersPart.trim().isEmpty();
        }

        /**
         * HTTP 바디를 파싱합니다.
         *
         * @param inputStream 소켓의 InputSteam
         * @param contentType 미디어 타입
         * */
        private void parseRequestBody(InputStream inputStream, String contentType) throws IOException {
            StringBuilder sb = new StringBuilder();
            int binary;
            int inputStreamLength = inputStream.available();
            byte[] data = new byte[inputStreamLength];
            int i = 0;
            while ((binary = inputStream.read()) != -1) {
                data[i] = (byte) binary;
                if (isNewLine(data, i) || inputStream.available() == 0) {
                    data = Arrays.copyOfRange(data, 0, ++i);
                    String line = new String(data, StandardCharsets.UTF_8);
                    sb.append(line);
                    data = new byte[inputStreamLength];
                    i = 0;
                }
                if (inputStream.available() == 0) break;
                i++;
            }
            if (ContentType.APPLICATION_JSON.getValue().equals(contentType) && this.parameters.isEmpty()) {
                this.json = sb.toString();
                return;
            }
            this.parameters = parseQuery(sb.toString());
        }

        /**
         * 한 줄의 끝인지 확인합니다.
         *
         * @param data 본문
         * @param index 인덱스
         * @return 한 줄의 끝인지에 대한 여부
         * */
        private boolean isNextLine(byte[] data, int index) {
            return index != 0 && data[index - 1] == '\r' && data[index] == '\n';
        }

        /**
         * HTTP 헤더를 파싱합니다.
         * 
         * @param headers 헤더
         * */
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

        /**
         * HTTP Method를 파싱합니다.
         * 
         * @param method HTTP Method 이름
         * */
        private void parseMethod(String method) {
            this.method = HttpMethod.get(method);
        }

        /**
         * 요청 URL을 파싱하여 저장하고 쿼리 스트링을 반환합니다.
         *
         * @param requestPath 요청 URL
         * @return 쿼리 스트링
         * */
        private String parseRequestPath(String requestPath) {
            int index = requestPath.indexOf("?");
            if (index != -1) {
                this.path = requestPath.substring(0, index);
                return requestPath.substring(index + 1);
            }
            this.path = requestPath;
            return "";
        }

        /**
         * 쿼리 스트링을 파싱합니다.
         * 
         * @param  parameters 쿼리 스트링
         * @return 파라미터 목록
         * */
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

        /**
         * multipart/form-data 요청을 파싱합니다.
         *
         * @param inputStream 소켓의 InputStream
         * @param boundary Multipart boundary
         * */
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

        /**
         * multipart/form-data 본문을 한 파트씩 파싱합니다.
         * 
         * @param inputStream 소켓의 InputStream
         * @param boundary 멀티파트의 boundary
         * @throws IOException InputStream을 읽다가 오류 발생시 
         * */
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
                if (isNewLine(data, i)) {
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
                        else this.parameters.put(name, value);

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

        /**
         * multipart/form-data로 받은 파일을 인스턴스로 만듭니다.
         * 
         * @param name 파일 이름
         * @param filename 파일 전체 이름
         * @param contentType 미디어 타입
         * @param fileData 파일의 데이터
         * @see org.sam.server.http.MultipartFile
         * */
        private void createMultipartFile(String name, String filename, String contentType, byte[] fileData) {
            MultipartFile multipartFile = new MultipartFile(filename, contentType, fileData);
            if (this.files.get(name) == null) {
                this.files.put(name, multipartFile);
                return;
            }
            Object file = this.files.get(name);
            addMultipartFileToList(name, multipartFile, file);
        }

        /**
         * MultipartFile을 추가합니다.
         *
         * @param name MultipartFile의 이름
         * @param multipartFile MultipartFile 인스턴스
         * @param file MultipartFile 목록 또는 MultipartFile
         * */
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

        /**
         * Multipart boundary를 기준으로 파일을 읽어 들이고 바이트 배열을 반환합니다.
         *
         * @param inputStream 소켓의 InputStream
         * @param boundary Multipart boundary
         * @return 파일의 바이트 배열
         * */
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
                if (isNewLine(data, fileLength)) {
                    String content = new String(data, StandardCharsets.UTF_8);
                    if (content.trim().equals(boundary)) return null;
                    boundary = new String(boundary.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                    int index = content.indexOf(boundary);
                    if (index != -1) break;
                }
                fileLength++;
            }
            return Arrays.copyOfRange(data, 2, fileLength - boundary.getBytes(StandardCharsets.UTF_8).length);
        }

        /**
         * HttpRequest 혹은 HttpMultipartRequest 인스턴스를 생성합니다.
         *
         * @return 요청 인스턴스
         * */
        public Request createRequest() {
            if (this.headers.isEmpty()) return null;
            Map<String, String> headers = this.headers;
            HttpMethod method = this.method;
            String path = this.path;
            Map<String, String> parameters = this.parameters;
            String json = this.json;
            Set<Cookie> cookies = this.cookies;
            String contentType = headers.get("content-type") != null ? headers.get("content-type") : "";
            if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue()))
                return new HttpMultipartRequest(protocol, path, method, headers, parameters, json, cookies, files);
            return new HttpRequest(protocol, path, method, headers, parameters, json, cookies);
        }

        /**
         *  한 줄의 마지막인지 확인합니다.
         *
         * @param data 데이터
         * @param index 인덱스
         * @return 한 줄의 마지막인지 여부
         * */
        private boolean isNewLine(byte[] data, int index) {
            return index != 0 && data[index - 1] == '\r' && data[index] == '\n';
        }

        /**
         *  헤더의 끝 부분인지 확인합니다.
         *
         * @param data 데이터
         * @return 헤더의 끝인지 여부
         * */
        private static boolean isCompleteHeader(String data) {
            return data.endsWith("\r\n\r\n");
        }
    }

}
