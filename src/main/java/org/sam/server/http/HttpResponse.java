package org.sam.server.http;

import org.sam.server.HttpServer;
import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;

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
public class HttpResponse {

    private final ClassLoader classLoader = getClass().getClassLoader();

    private static final String DEFAULT_FILE = "static/index.html";
    private static final String BAD_REQUEST = "static/400.html";
    private static final String FILE_NOT_FOUND = "static/404.html";
    private static final String METHOD_NOT_SUPPORTED = "static/not_supported";

    private final PrintWriter writer;
    private final BufferedOutputStream outputStream;
    private final Map<String, Object> headers = new HashMap<>();
    private Set<Cookie> cookies = CookieStore.getCookies();
    private final String requestPath;

    private String filePath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    public HttpResponse(OutputStream os, String path) {
        this.writer = new PrintWriter(os);
        this.outputStream = new BufferedOutputStream(os);
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
                fileLength = readJson(filePath);
            } else  fileLength = loadStaticFile(filePath);

            headers.put("Server", "Java HTTP Server from sam : 1.0");
            headers.put("Date", LocalDateTime.now());
            headers.put("Content-Type", getContentMimeType());
            headers.put("Content-length", fileLength);

            printHeader();

            CookieStore.vacateList();
            writer.flush();
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addCookies(Cookie cookie) {
        this.cookies.add(cookie);
    }

    private int loadStaticFile(String filePath) throws IOException {
        InputStream fis = classLoader.getResourceAsStream(filePath);
        File staticFile = new File("src/main" + filePath);

        if (fis == null && !staticFile.exists()) {
            fileNotFound();
            return 0;
        }

        byte[] fileData = new byte[1024];
        int fileLength;
        if (staticFile.exists()) {
            fileLength = (int) staticFile.length();
            fileData = readFileData(staticFile, fileLength);
        } else {
            assert fis != null;
            fileLength = fis.read(fileData);
        }

        outputStream.write(fileData, 0, fileLength);

        return fileLength;
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = new FileInputStream(file);
        fileIn.read(fileData);
        return fileData;
    }

    private int readJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) ||
                httpStatus.equals(HttpStatus.BAD_REQUEST)) return 0;

        byte[] bytes = json.getBytes();
        outputStream.write(bytes);
        return bytes.length;
    }

    private void printHeader() {
        writer.println("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage());
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
                httpStatus.equals(HttpStatus.NOT_IMPLEMENTED) ||
                this.requestPath.endsWith(".html")) return ContentType.TEXT_HTML.getValue();
        if (requestPath.endsWith(".css")) return ContentType.CSS.getValue();
        if (requestPath.endsWith(".js")) return ContentType.JAVASCRIPT.getValue();

        return ContentType.TEXT_PLAIN.getValue();
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
}
