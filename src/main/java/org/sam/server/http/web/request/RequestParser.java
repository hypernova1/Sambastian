package org.sam.server.http.web.request;

import org.sam.server.common.ArrayUtils;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.http.Cookie;
import org.sam.server.http.CookieStore;
import org.sam.server.util.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 소켓으로 부터 받은 InputStream을 읽어 Request 인스턴스를 생성하는 클래스
 *
 * @author hypernova1
 * @see Request
 * @see HttpRequest
 * @see HttpMultipartRequest
 */
public final class RequestParser {
    String protocol;
    String url;
    HttpMethod httpMethod;
    Map<String, String> headers = new HashMap<>();
    Map<String, String> parameters = new HashMap<>();
    Set<Cookie> cookies = new HashSet<>();
    Map<String, Object> files = new HashMap<>();
    String json;
    private ContentType contentType;
    private String boundary;

    /**
     * InputStream에서 HTTP 본문을 읽은 후 파싱한다.
     *
     * @param in 소켓의 InputStream
     */
    void parse(InputStream in) {
        BufferedInputStream inputStream = new BufferedInputStream(in);
        String headersPart = parseHeaderPart(inputStream);

        if (isNonHttpRequest(headersPart)) return;

        String[] headers = headersPart.split("\r\n");
        StringTokenizer tokenizer = new StringTokenizer(headers[0]);
        String httpMethodPart = tokenizer.nextToken().toUpperCase();
        String requestUrl = tokenizer.nextToken().toLowerCase();

        this.protocol = tokenizer.nextToken().toUpperCase();
        this.headers = parseHeaders(headers);
        this.httpMethod = HttpMethod.valueOf(httpMethodPart);
        this.contentType = parseContentType();

        String query = parseRequestUrl(requestUrl);

        if (StringUtils.isNotEmpty(query)) {
            this.parameters = parseQuery(query);
        }

        if (this.httpMethod.hasBody()) {
            parseBody(inputStream);
        }
    }

    private ContentType parseContentType() {
        String contentType = this.headers.getOrDefault("content-type", "text/plain");
        ContentType result = ContentType.get(contentType);
        if (contentType.startsWith(ContentType.MULTIPART_FORM_DATA.getValue())) {
            this.boundary = "--" + contentType.split("; ")[1].split("=")[1];
            this.contentType = ContentType.get(contentType.split("; ")[0]);
        }
        return result;
    }

    /**
     * HTTP 바디에 있는 데이터를 파싱한다.
     *
     * @param inputStream 인풋 스트림
     */
    private void parseBody(BufferedInputStream inputStream) {
        if (this.boundary != null) {
            parseMultipartBody(inputStream);
            return;
        }
        parseRequestBody(inputStream);
    }

    /**
     * HTTP 헤더를 읽어 반환한다.
     *
     * @param inputStream 인풋 스트림
     * @return HTTP 헤더 내용
     */
    private String parseHeaderPart(BufferedInputStream inputStream) {
        int i;
        String headersPart = "";
        StringBuilder sb = new StringBuilder();
        try {
            while ((i = inputStream.read()) != -1) {
                char c = (char) i;
                sb.append(c);
                if (isEndOfHeader(sb.toString())) {
                    headersPart = sb.toString().replace("\r\n\r\n", "");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return headersPart;
    }

    /**
     * HTTP 바디를 파싱한다.
     *
     * @param inputStream 소켓의 InputSteam
     */
    private void parseRequestBody(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try {
            int binary;
            int inputStreamLength = inputStream.available();
            byte[] data = new byte[inputStreamLength];
            int i = 0;
            while ((binary = inputStream.read()) != -1) {
                data[i] = (byte) binary;
                if (isEndOfLine(data, i) || inputStream.available() == 0) {
                    data = Arrays.copyOfRange(data, 0, i + 1);
                    String line = new String(data, StandardCharsets.UTF_8);
                    sb.append(line);
                    data = new byte[inputStreamLength];
                    i = 0;
                }
                if (inputStream.available() == 0) break;
                i++;
            }
            if (isJsonRequest()) {
                this.json = sb.toString();
                return;
            }
            this.parameters = parseQuery(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * HTTP 헤더를 파싱한다.
     *
     * @param headers 헤더 본문
     * @return 헤더 목록
     */
    private Map<String, String> parseHeaders(String[] headers) {
        Map<String, String> result = new HashMap<>();
        for (int i = 1; i < headers.length; i++) {
            int index = headers[i].indexOf(": ");
            String key = headers[i].substring(0, index).toLowerCase();
            String value = headers[i].substring(index + 2);
            if ("cookie".equals(key)) {
                this.cookies = CookieStore.parseCookie(value);
                continue;
            }
            result.put(key, value);
        }
        return result;
    }

    /**
     * 요청 URL을 파싱하여 저장하고 쿼리 스트링을 반환한다.
     *
     * @param url 요청 URL
     * @return 쿼리 스트링
     */
    private String parseRequestUrl(String url) {
        int index = url.indexOf("?");
        if (index == -1) {
            this.url = url;
            return "";
        }
        this.url = url.substring(0, index);
        return url.substring(index + 1);
    }

    /**
     * 쿼리 스트링을 파싱한다.
     *
     * @param parameters 쿼리 스트링
     * @return 파라미터 목록
     */
    private Map<String, String> parseQuery(String parameters) {
        Map<String, String> map = new HashMap<>();
        String[] rawParameters = parameters.split("&");
        for (String rawParameter : rawParameters) {
            String[] parameterPair = rawParameter.split("=");
            String name = parameterPair[0];
            String value = "";
            if (existsParameterValue(parameterPair)) {
                value = parameterPair[1];
            }
            map.put(name, value);
        }
        return map;
    }

    /**
     * multipart/form-data 요청을 파싱한다.
     *
     * @param inputStream 소켓의 InputStream
     */
    private void parseMultipartBody(InputStream inputStream) {
        try {
            StringBuilder sb = new StringBuilder();
            int i;
            while ((i = inputStream.read()) != -1) {
                sb.append((char) i);
                if (isBoundaryLine(sb.toString())) {
                    parseMultipartLine(inputStream);
                    return;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * multipart/form-data 본문을 한 파트씩 파싱한다.
     *
     * @param inputStream 소켓의 InputStream
     * @throws IOException InputStream을 읽다가 오류 발생시
     */
    private void parseMultipartLine(InputStream inputStream) throws IOException {
        int i = 0;
        int loopCnt = 0;
        String name = "";
        String value = "";
        String filename = "";
        String mimeType = "";
        byte[] fileData = null;
        boolean isFile = false;
        int inputStreamLength = inputStream.available();
        byte[] data = new byte[inputStreamLength];
        int binary;
        while ((binary = inputStream.read()) != -1) {
            data[i] = (byte) binary;
            if (isEndOfLine(data, i)) {
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
                    mimeType = line.substring(index + 2);
                    fileData = parseFile(inputStream);
                    loopCnt = 0;
                    if (fileData == null) continue;
                    line = boundary;
                } else if (loopCnt == 1 && !line.contains(boundary)) {
                    value = line;
                    loopCnt = 0;
                    continue;
                }

                if (line.contains(boundary)) {
                    if (!filename.isEmpty()) {
                        createMultipartFile(name, filename, mimeType, fileData);
                    } else {
                        this.parameters.put(name, value);
                    }

                    name = "";
                    value = "";
                    filename = "";
                    mimeType = "";
                    fileData = null;
                    loopCnt = 0;
                }
                if (inputStream.available() == 0) return;
            }
            i++;
        }
    }

    /**
     * multipart/form-data로 받은 파일을 인스턴스로 만든다.
     *
     * @param name     파일 이름
     * @param filename 파일 전체 이름
     * @param mimeType 미디어 타입
     * @param fileData 파일의 데이터
     * @see MultipartFile
     */
    private void createMultipartFile(String name, String filename, String mimeType, byte[] fileData) {
        MultipartFile multipartFile = new MultipartFile(filename, mimeType, fileData);
        if (this.files.get(name) == null) {
            this.files.put(name, multipartFile);
            return;
        }
        Object file = this.files.get(name);
        addMultipartFile(name, multipartFile, file);
    }

    /**
     * MultipartFile을 추가한다.
     *
     * @param name          MultipartFile의 이름
     * @param multipartFile MultipartFile 인스턴스
     * @param file          MultipartFile 목록 또는 MultipartFile
     */
    @SuppressWarnings("unchecked")
    private void addMultipartFile(String name, MultipartFile multipartFile, Object file) {
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
     * Multipart boundary를 기준으로 파일을 읽어 들이고 바이트 배열을 반환한다.
     *
     * @param inputStream 소켓의 InputStream
     * @return 파일의 바이트 배열
     */
    private byte[] parseFile(InputStream inputStream) {
        byte[] data = new byte[1024 * 8];
        int fileLength = 0;
        try {
            int i;
            while ((i = inputStream.read()) != -1) {
                if (ArrayUtils.isFullCapacity(data, fileLength)) {
                    data = ArrayUtils.getDoubleArray(data);
                }
                data[fileLength] = (byte) i;
                if (isEndOfLine(data, fileLength)) {
                    String content = new String(data, StandardCharsets.UTF_8);
                    if (isEmptyBoundaryContent(content)) return null;
                    if (isEndOfBoundaryLine(content)) break;
                }
                fileLength++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.copyOfRange(data, 2, fileLength - boundary.getBytes(StandardCharsets.UTF_8).length);
    }

    /**
     * HttpRequest 혹은 HttpMultipartRequest 인스턴스를 생성한다.
     *
     * @return 요청 인스턴스
     * @see HttpRequest
     * @see HttpMultipartRequest
     */
    public Request createRequest() {
        if (headers.isEmpty()) {
            return null;
        }

        if (contentType == ContentType.MULTIPART_FORM_DATA) {
            return new HttpMultipartRequest(this);
        }

        return new HttpRequest(this);
    }

    /**
     * 한 줄의 마지막인지 확인한다.
     *
     * @param data  데이터
     * @param index 인덱스
     * @return 한 줄의 마지막인지 여부
     */
    private boolean isEndOfLine(byte[] data, int index) {
        return index != 0 && data[index - 1] == '\r' && data[index] == '\n';
    }

    /**
     * 헤더의 끝 부분인지 확인한다.
     *
     * @param data 데이터
     * @return 헤더의 끝인지 여부
     */
    private static boolean isEndOfHeader(String data) {
        String CR = "\r";
        return data.endsWith(CR + "\n\r\n");
    }

    /**
     * 파라미터에 값이 있는지 확인한다.
     *
     * @param parameterPair 파라미터 쌍
     * @return 파라미터 값 존재 여부
     */
    private boolean existsParameterValue(String[] parameterPair) {
        return parameterPair.length == 2;
    }

    /**
     * HTTP 요청이 아닌지 확인한다.
     *
     * @param headersPart 헤더
     * @return HTTP 요청이 아닌지에 대한 여부
     */
    private boolean isNonHttpRequest(String headersPart) {
        return headersPart.trim().isEmpty();
    }

    /**
     * HTTP 요청 본문이 JSON인지 확인한다.
     */
    private boolean isJsonRequest() {
        return this.contentType == ContentType.APPLICATION_JSON && this.parameters.isEmpty();
    }

    /**
     * boundary 라인인지 확인한다.
     *
     * @param line multipart 본문 라인
     * @return boundary 라인 여부
     */
    private boolean isBoundaryLine(String line) {
        return line.contains(this.boundary + "\r\n");
    }

    /**
     * boundary 라인이 끝났는지 확인한다.
     *
     * @param content multipart 본문 라인
     * @return boundary 라인이 끝났는지 여부
     */
    private boolean isEndOfBoundaryLine(String content) {
        String boundary = new String(this.boundary.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        return content.contains(boundary);
    }

    /**
     * boundary 라인이 존재하는 지 확인한다.
     *
     * @param content multipart 본문 라인
     * @return boundary 라인 존재 여부
     */
    private boolean isEmptyBoundaryContent(String content) {
        return content.trim().equals(this.boundary);
    }

}