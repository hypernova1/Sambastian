package org.sam.server.http;

import org.sam.server.HttpServer;
import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class HttpResponse {

    private static final String DEFAULT_FILE = "static/index.html";
    private static final String BAD_REQUEST = "static/400.html";
    private static final String FILE_NOT_FOUND = "static/404.html";
    private static final String METHOD_NOT_SUPPORTED = "static/not_supported";

    private final ClassLoader classLoader = getClass().getClassLoader();

    private final PrintWriter out;
    private final BufferedOutputStream bos;
    private final Map<String, Object> headers = new HashMap<>();
    private Set<Cookie> cookies = CookieStore.getCookies();
    private final String requestPath;

    private String filePath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    public HttpResponse(OutputStream os, String path) {
        this.out = new PrintWriter(os);
        this.bos = new BufferedOutputStream(os);
        this.requestPath = path;
    }

    public static HttpResponse create(OutputStream os, String path) {
        return new HttpResponse(os, path);
    }

    public void execute(String filePath, HttpStatus status) {
        this.httpStatus = status;

        int fileLength;
        try {
            if (getContentMimeType().equals(ContentType.APPLICATION_JSON.getValue())) {
                fileLength = loadJson(filePath);
            } else {
                fileLength = loadStaticFile(filePath);
            }

            headers.put("Server", "Java HTTP Server from sam : 1.0");
            headers.put("Date", LocalDateTime.now());
            headers.put("Content-Type", getContentMimeType());
            headers.put("Content-length", fileLength);

            printHeader();

            CookieStore.vacateList();

            out.flush();
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCookies(Cookie cookie) {
        this.cookies.add(cookie);
    }

    private int loadStaticFile(String filePath) throws IOException {
        InputStream fis = classLoader.getResourceAsStream(filePath);

        if (fis == null) {
            fileNotFound();
            return 0;
        }

        byte[] fileData = new byte[1024];
        int fileLength = fis.read(fileData);
        bos.write(fileData, 0, fileLength);

        return fileLength;
    }

    private int loadJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) ||
                httpStatus.equals(HttpStatus.BAD_REQUEST)) return 0;

        byte[] bytes = json.getBytes();
        bos.write(bytes);
        return bytes.length + 1;
    }

    private void printHeader() {
        out.println("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage());
        headers.keySet().forEach(key -> out.println(key + ": " + headers.get(key)));
        printCookies();
        out.println();
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
            out.println(line.toString());
        }
    }

    public void fileNotFound() {
        if (HttpServer.verbose) {
            System.out.println("File " + requestPath + " not found");
        }
        execute(FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    public void badRequest() {
        if (HttpServer.verbose) {
            System.out.println("Bad Request");
        }
        execute(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    public void methodNotImplemented() throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + requestPath + "method");
        }

        execute(METHOD_NOT_SUPPORTED, HttpStatus.NOT_IMPLEMENTED);
    }

    public void returnIndexFile() throws IOException {
        if (this.requestPath.endsWith("/")) {
            filePath = DEFAULT_FILE;
        }

        execute(filePath, HttpStatus.OK);
    }

    public void setContentMimeType(ContentType contentMimeType) {
        this.contentMimeType = contentMimeType.getValue();
    }

    public String getContentMimeType() {
        if (contentMimeType != null) return contentMimeType;
        if (httpStatus.equals(HttpStatus.NOT_FOUND) ||
                httpStatus.equals(HttpStatus.BAD_REQUEST) ||
                httpStatus.equals(HttpStatus.NOT_IMPLEMENTED)) return "text/html";
        if (this.requestPath.endsWith(".html")) return "text/html";
        return "text/plain";
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

}
