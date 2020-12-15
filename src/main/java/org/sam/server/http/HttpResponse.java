package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.ResourcesNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 요청을 해석하고 응답하는 클래스입니다. 정적 자원을 반환합니다.
 *
 * @author hypernova1
 * @see #execute(String, HttpStatus) 
 */
public class HttpResponse implements Response {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private static final String DEFAULT_FILE = "static/index.html";
    private static final String BAD_REQUEST = "static/400.html";
    private static final String NOT_FOUND = "static/404.html";
    private static final String FAVICON = "favicon.ico";
    private static final String METHOD_NOT_ALLOWED = "static/method_not_allowed.html";

    private final static String BUFFER_SIZE_PROPERTY = ServerProperties.get("file-buffer-size");

    private final PrintWriter writer;

    private  final BufferedOutputStream outputStream;

    private final Map<String, Object> headers = new HashMap<>();

    private final Set<Cookie> cookies = CookieStore.getCookies();

    private final String requestPath;

    private final HttpMethod requestMethod;

    private final Set<HttpMethod> allowedMethods = new LinkedHashSet<>();

    private String filePath;

    private HttpStatus httpStatus;

    private String contentMimeType;

    private long fileLength;

    {
        headers.put("Accept-Ranges", "bytes");
        headers.put("Connection", "Keep-Alive");
        headers.put("Keep-Alive", "timeout=60");
    }

    private HttpResponse(OutputStream os, String path, HttpMethod requestMethod) {

        int bufferSize = BUFFER_SIZE_PROPERTY != null ? Integer.parseInt(BUFFER_SIZE_PROPERTY) : 8192;
        this.writer = new PrintWriter(os);
        this.outputStream = new BufferedOutputStream(os, bufferSize);
        this.requestPath = path;
        this.requestMethod = requestMethod;
    }

    /**
     * 인스턴스를 생성합니다.
     *
     * @author hypernova1
     * @param os 응답을 출력할 스트림
     * @param requestPath 요청 URL
     * @param requestMethod 요청 HTTP Method
     * @return HttpResponse 인스턴스
     * */
    static HttpResponse create(OutputStream os, String requestPath, HttpMethod requestMethod) {
        return new HttpResponse(os, requestPath, requestMethod);
    }

    @Override
    public void execute(String pathOrJson, HttpStatus status) {
        this.httpStatus = status;
        try {
            if (getContentMimeType().equals(ContentType.APPLICATION_JSON.getValue())) {
                if (!requestMethod.equals(HttpMethod.OPTIONS)) {
                    this.fileLength = readJson(pathOrJson);
                }
            } else if (allowedMethods.isEmpty()) {
                this.fileLength = readStaticResource(pathOrJson);
            }
            printHeader();
            CookieStore.vacateList();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            writer.flush();
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer.close();
        }
    }

    /**
     * 정적 자원의 경로를 받아 파일을 읽습니다. 파일이 존재하지 않으면 notFound 메서드를 호출합니다.
     *
     * @param filePath 파일 경로
     * @return 파일의 길이
     * @see #notFound()
     * @see #readFileData(File)
     * @see #writeStaticFile(InputStream)  
     * */
    private long readStaticResource(String filePath) {
        InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        File staticFile = new File("src/main" + filePath);
        if (fis == null && !staticFile.exists()) {
            notFound();
            return 0;
        }
        if (!filePath.equals(NOT_FOUND) && requestMethod.equals(HttpMethod.OPTIONS)) {
            allowedMethods.add(HttpMethod.GET);
            return 0;
        }
        long fileLength = 0;
        try {
            if (staticFile.exists()) {
                fileLength = readFileData(staticFile);
            } else {
                assert fis != null;
                fileLength = writeStaticFile(fis);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileLength;
    }

    /**
     * 정적 파일을 읽은 후 OutputStream에 쓰고 파일의 길이를 반환합니다.
     *
     * @param fis 파일을 읽은 스트림
     * @return 파일의 길이
     * @throws IOException 파일을 읽다가 오류 발생시
     * */
    private long writeStaticFile(InputStream fis) throws IOException {
        long fileLength = 0;
        int i;
        while ((i = fis.read()) != -1) {
            if (!this.requestMethod.equals(HttpMethod.HEAD)) {
                outputStream.write(i);
            }
            fileLength++;
        }
        return fileLength;
    }

    /**
     * 정적 파일을 읽은 후 OutputStream에 쓰고 파일의 길이를 반환합니.
     *
     * @param file 정적 파일
     * @return 파일의 길이
     * @throws IOException 파일을 읽다가 문제 발생시
     * */
    private long readFileData(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int len;
        if (!this.requestMethod.equals(HttpMethod.HEAD)) {
            byte[] buf = new byte[fis.available()];
            while ((len = fis.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
        }
        fis.close();

        return file.length();
    }

    /**
     * JSON 문자열을 OutputStream에 쓰고 바이트 길이를 반환합니다.
     *
     * @param json JSON 문자열
     * @return JSON 문자열의 바이트 길이
     * @throws IOException 문자열을 읽다가 오류 발생시
     * */
    private int readJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST)) {
            return 0;
        }

        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        if (!this.requestMethod.equals(HttpMethod.HEAD)) {
            outputStream.write(bytes);
        }

        return bytes.length;
    }

    /**
     * 응답 헤더를 OutputStream에 씁니다.
     * */
    private void printHeader() {
        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());
        headers.put("Content-length", this.fileLength);

        if (requestPath.startsWith("/resources")) {
            headers.put("Cache-Control", "max-age=86400");
        } else {
            headers.put("Cache-Control", "no-cache, no-store, must-revalidate");
        }

        if (requestMethod.equals(HttpMethod.OPTIONS) && allowedMethods.size() > 0) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            allowedMethods.forEach(allowedMethod -> stringJoiner.add(allowedMethod.toString()));
            headers.put("Allow", stringJoiner.toString());
        }

        writer.print("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage() + "\r\n");
        headers.keySet().forEach(key -> writer.print(key + ": " + headers.get(key) + "\r\n"));
        printCookies();
        writer.print("\r\n");
    }

    /**
     * 쿠키에 대한 정보를 OutputStream에 씁니다.
     *
     * @see org.sam.server.http.Cookie
     * */
    private void printCookies() {
        for (Cookie cookie : cookies) {
            StringBuilder line = new StringBuilder();
            line.append("Set-Cookie: ");
            line.append(cookie.getName()).append("=").append(cookie.getValue());
            if (cookie.getMaxAge() != 0) {
                line.append("; Expires=").append(cookie.getExpires());
                line.append("; Max-Age=").append(cookie.getMaxAge());
            }
            if (ServerProperties.isSSL()) {
                line.append("; Secure");
            }
            if (cookie.isHttpOnly()) {
                line.append("; HttpOnly");
            }
            line.append("; Path=").append(cookie.getPath());
            writer.print(line.toString() + "\r\n");
        }
    }

    /**
     * 응답할 미디어 타입을 설정합니다.
     *
     * @param contentMimeType 미디어 타입
     * @see org.sam.server.constant.ContentType
     * */
    public void setContentMimeType(ContentType contentMimeType) {
        this.contentMimeType = contentMimeType.getValue();
    }

    /**
     * 조건에 따라 미디어타입을 반환합니다.
     *
     * @return 미디어 타입
     * @see org.sam.server.constant.ContentType
     * @see org.sam.server.constant.HttpStatus
     * */
    public String getContentMimeType() {
        if (contentMimeType != null) return contentMimeType;
        if (isHtmlResponse()) return ContentType.TEXT_HTML.getValue();
        if (requestPath.endsWith(".css")) return ContentType.CSS.getValue();
        if (requestPath.endsWith(".js")) return ContentType.JAVASCRIPT.getValue();

        return ContentType.TEXT_PLAIN.getValue();
    }

    /**
     * 응답할 MIME 형식이 HTML인지 확인합니다
     *
     * @return HTML 여부
     * */
    private boolean isHtmlResponse() {
        return httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST) ||
                httpStatus.equals(HttpStatus.NOT_IMPLEMENTED) || this.requestPath.endsWith(".html");
    }

    /**
     * 쿠키 정보를 추가합니다.
     *
     * @param cookie 추가할 쿠키
     * @see org.sam.server.http.Cookie
     * */
    public void addCookies(Cookie cookie) {
        this.cookies.add(cookie);
    }

    /**
     * 헤더 정보를 추가합니다.
     *
     * @param key 헤더명
     * @param value 헤더값
     * @see org.sam.server.constant.HttpHeader
     * */
    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    /**
     * 헤더 정보를 반환합니다.
     *
     * @param key 헤더명
     * @return 헤더
     * @see org.sam.server.constant.HttpHeader
     * */
    public Object getHeader(String key) {
        return headers.get(key);
    }

    /**
     * 모든 헤더의 이름을 반환합니다.
     *
     * @return 헤더 이름 리스트
     * @see org.sam.server.constant.HttpHeader
     * */
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    /**
     * 정적 자원에 대한 처리를 합니다.
     * 
     * @see #execute(String, HttpStatus) 
     * */
    protected void responseStaticResources() {
        String filePath = requestPath.replace("/resources", "/resources/static");
        execute(filePath, HttpStatus.OK);
    }

    /**
     * 찾는 정적 자원이 존재하지 않을시 처리합니다.
     * 
     * @see #execute(String, HttpStatus) 
     * */
    protected void notFound() {
        logger.warn("File " + requestPath + " not found");
        execute(NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    /**
     * 잘못된 요청에 대한 처리를 합니다.
     * 
     * @see #execute(String, HttpStatus) 
     * */
    protected void badRequest() {
        logger.warn("Bad Request");
        execute(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    /**
     * 요청한 URL이 일치하는 핸들러는 있지만 HTTP Method가 일치하지 않을 때에 대한 처리를 합니다.
     * 
     * @see #execute(String, HttpStatus)
     * */
    protected void methodNotAllowed() {
        logger.warn("Method Not Allowed");
        execute(METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED);
    }

    /**
     * 루트 경로의 요청을 처리합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    protected void responseIndexFile() {
        if (this.requestPath.endsWith("/"))
            filePath = DEFAULT_FILE;
        this.contentMimeType = ContentType.TEXT_HTML.getValue();
        execute(filePath, HttpStatus.OK);
    }

    /**
     * 파비콘에 대한 요청을 처리 합니다.
     *
     * @see #execute(String, HttpStatus)
     * */
    protected void responseFavicon() throws ResourcesNotFoundException {
        filePath = FAVICON;
        this.contentMimeType = ContentType.X_ICON.getValue();
        execute(filePath, HttpStatus.OK);
    }

    /**
     * OPTION Method으로 요청이 왔을 시 해당 URL로 사용할 수 있는 HttpMethod를 추가합니다.
     *
     * @param httpMethod 추가할 HTTP Method
     * @see org.sam.server.constant.HttpMethod
     * */
    public void addAllowedMethod(HttpMethod httpMethod) {
        this.allowedMethods.add(httpMethod);
    }

    /**
     * OPTION Method에 대한 요청을 처리합니다.
     * 
     * @see #execute(String, HttpStatus)
     * */
    public void executeOptionsResponse() {
        if (allowedMethods.isEmpty()) {
            this.notFound();
            return;
        }
        this.execute(null, HttpStatus.OK);
    }
}
