package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpResponse extends Response {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private final Map<String, Object> headers = new HashMap<>();
    private Set<Cookie> cookies = CookieStore.getCookies();
    private final String requestPath;

    private String filePath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    private byte[] fileData = new byte[1024];
    private int fileLength;

    public HttpResponse(OutputStream os, String path) {
        super(os);
        this.requestPath = path;
    }

    public static HttpResponse create(OutputStream os, String path) {
        return new HttpResponse(os, path);
    }

    public void execute(String filePath, HttpStatus status) {
        this.httpStatus = status;

        try {
            if (getContentMimeType().equals(ContentType.APPLICATION_JSON.getValue())) {
                this.fileLength = readJson(filePath);
            } else  this.fileLength = readStaticResource(filePath);

            printHeader();

            outputStream.write(fileData, 0, this.fileLength);
            CookieStore.vacateList();
            writer.flush();
            outputStream.flush();
            outputStream.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private int readStaticResource(String filePath) throws IOException {
        InputStream fis = classLoader.getResourceAsStream(filePath);
        File staticFile = new File("src/main" + filePath);

        if (fis == null && !staticFile.exists()) {
            fileNotFound();
            return 0;
        }

        int fileLength;
        if (staticFile.exists()) {
            fileLength = readFileData(staticFile);
        } else {
            assert fis != null;
            fileLength = fis.read(fileData);
        }

        return fileLength;
    }

    private int readFileData(File file) throws IOException {
        FileInputStream fileIn = new FileInputStream(file);
        return fileIn.read(this.fileData);
    }

    private int readJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST))
            return 0;

        byte[] bytes = json.getBytes();
        outputStream.write(bytes);
        return bytes.length;
    }

    private void printHeader() {
        writer.println("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage());
        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());
        headers.put("Content-length", fileLength);
        headers.keySet().forEach(key -> writer.println(key + ": " + headers.get(key)));
        printCookies();
        writer.println();
    }

    private void printCookies() {
        for (Cookie cookie : cookies) {
            StringBuilder line = new StringBuilder();
            line.append("Set-Cookie: ");
            line.append(cookie.getName()).append("=").append(cookie.getValue());
            if (cookie.getMaxAge() != 0) {
                line.append("; Expires=").append(cookie.getExpires());
                line.append("; Max-Age=").append(cookie.getMaxAge());
            }
            if (ServerProperties.IS_SSL) {
                line.append("; Secure");
            }
            if (cookie.isHttpOnly()) {
                line.append("; HttpOnly");
            }

            line.append("; Path=").append(cookie.getPath());
            writer.println(line.toString());
        }
    }

    public void setContentMimeType(ContentType contentMimeType) {
        this.contentMimeType = contentMimeType.getValue();
    }

    public String getContentMimeType() {
        if (contentMimeType != null) return contentMimeType;
        if (httpStatus.equals(HttpStatus.NOT_FOUND) ||
                httpStatus.equals(HttpStatus.BAD_REQUEST) ||
                httpStatus.equals(HttpStatus.NOT_IMPLEMENTED) ||
                this.requestPath.endsWith(".html")) return ContentType.TEXT_HTML.getValue();
        if (requestPath.endsWith(".css")) return ContentType.CSS.getValue();
        if (requestPath.endsWith(".js")) return ContentType.JAVASCRIPT.getValue();

        return ContentType.TEXT_PLAIN.getValue();
    }

    public void addCookies(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    public void getStaticResources() {
        String filePath = requestPath.replace("/resources", "/resources/static");
        execute(filePath, HttpStatus.OK);
    }

    public void fileNotFound() {
        logger.warn("File " + requestPath + " not found");
        execute(FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public void badRequest() {
        logger.warn("Bad Request");
        execute(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public void methodNotImplemented() throws IOException {
        logger.warn("501 not implemented :" + requestPath + "method");
        execute(METHOD_NOT_SUPPORTED, HttpStatus.NOT_IMPLEMENTED);
    }

    public void returnIndexFile() throws IOException {
        if (this.requestPath.endsWith("/")) {
            filePath = DEFAULT_FILE;
        }

        execute(filePath, HttpStatus.OK);
    }
}
