package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.ResourcesNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
@SuppressWarnings("unused")
public class HttpResponse extends Response {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

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
        super(os);
        this.requestPath = path;
        this.requestMethod = requestMethod;
    }

    protected static HttpResponse create(OutputStream os, String path, HttpMethod requestMethod) {
        return new HttpResponse(os, path, requestMethod);
    }

    protected void execute(String filePath, HttpStatus status) {
        this.httpStatus = status;
        try {
            if (getContentMimeType().equals(ContentType.APPLICATION_JSON.getValue())) {
                if (!requestMethod.equals(HttpMethod.OPTIONS)) {
                    this.fileLength = readJson(filePath);
                }
            } else if (allowedMethods.isEmpty()) {
                this.fileLength = readStaticResource(filePath);
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

    private long readStaticResource(String filePath) throws ResourcesNotFoundException {
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
        } catch (IOException | ResourcesNotFoundException e) {
            e.printStackTrace();
        }
        return fileLength;
    }

    private long writeStaticFile(InputStream fis) throws ResourcesNotFoundException {
        long fileLength = 0;
        try {
            int i;
            while ((i = fis.read()) != -1) {
                if (!this.requestMethod.equals(HttpMethod.HEAD)) {
                    outputStream.write(i);
                }
                fileLength++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return fileLength;
    }

    private long readFileData(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int fileLength = 0;
        int len;
        if (!this.requestMethod.equals(HttpMethod.HEAD)) {
            byte[] buf = new byte[fis.available()];
            while ((len = fis.read(buf)) > 0) {
                outputStream.write(buf, 0, len);
            }
            fis.close();
        }

        return file.length();
    }

    private int readJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST))
            return 0;

        byte[] bytes = json.getBytes();
        if (!this.requestMethod.equals(HttpMethod.HEAD)) {
            outputStream.write(bytes);
        }
        return bytes.length;
    }

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

    protected void responseStaticResources() {
        String filePath = requestPath.replace("/resources", "/resources/static");
        execute(filePath, HttpStatus.OK);
    }

    protected void notFound() {
        logger.warn("File " + requestPath + " not found");
        execute(NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    protected void badRequest() {
        logger.warn("Bad Request");
        execute(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    protected void methodNotAllowed() {
        logger.warn("Method Not Allowed");
        execute(METHOD_NOT_ALLOWED, HttpStatus.METHOD_NOT_ALLOWED);
    }

    protected void responseIndexFile() {
        if (this.requestPath.endsWith("/"))
            filePath = DEFAULT_FILE;
        this.contentMimeType = ContentType.TEXT_HTML.getValue();
        execute(filePath, HttpStatus.OK);
    }

    protected void getFavicon() throws ResourcesNotFoundException {
        filePath = FAVICON;
        this.contentMimeType = ContentType.X_ICON.getValue();
        execute(filePath, HttpStatus.OK);
    }

    public void addAllowedMethod(HttpMethod httpMethod) {
        this.allowedMethods.add(httpMethod);
    }

    public void executeOptionsResponse() {
        if (allowedMethods.isEmpty()) {
            this.notFound();
            return;
        }
        this.execute(null, HttpStatus.OK);
    }
}
