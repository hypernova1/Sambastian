package org.sam.server.http;

import org.sam.server.common.ServerProperties;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.ResourcesNotFoundException;
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
@SuppressWarnings("unused")
public class HttpResponse extends Response {

    private static final Logger logger = LoggerFactory.getLogger(HttpResponse.class);

    private final Map<String, Object> headers = new HashMap<>();
    private final Set<Cookie> cookies = CookieStore.getCookies();
    private final String requestPath;

    private String filePath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    private long fileLength;

    private HttpResponse(OutputStream os, String path) {
        super(os);
        this.requestPath = path;
    }

    protected static HttpResponse create(OutputStream os, String path) {
        return new HttpResponse(os, path);
    }

    protected void execute(String filePath, HttpStatus status) {
        this.httpStatus = status;
        try {
            if (getContentMimeType().equals(ContentType.APPLICATION_JSON.getValue()))
                this.fileLength = readJson(filePath);
            else
                this.fileLength = readStaticResource(filePath);
            printHeader();
            CookieStore.vacateList();
            writer.flush();
            outputStream.flush();
            outputStream.close();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private long readStaticResource(String filePath) throws ResourcesNotFoundException {
        InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream(filePath);
        File staticFile = new File("src/main" + filePath);
        if (fis == null && !staticFile.exists()) {
            fileNotFound();
            return 0;
        }
        long fileLength = 0;
        try {
            if (staticFile.exists()) {
                fileLength = readFileData(staticFile);
            } else {
                int i;
                assert fis != null;
                while ((i = fis.read()) != -1) {
                    outputStream.write(i);
                    fileLength++;
                }
            }
        } catch (IOException e) {
            throw new ResourcesNotFoundException(filePath);
        }
        return fileLength;
    }

    private long readFileData(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int fileLength = 0;
        int len;
        byte[] buf = new byte[fis.available()];
        while ((len = fis.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }
        fis.close();

        return file.length();
    }

    private int readJson(String json) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST))
            return 0;

        byte[] bytes = json.getBytes();
        outputStream.write(bytes);
        return bytes.length;
    }

    private void printHeader() {
        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());
        headers.put("Content-length", this.fileLength);
        headers.put("Accept-Ranges", "bytes");
        headers.put("Connection", "Keep-Alive");
        headers.put("Keep-Alive", "timeout=60");
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

    protected void getStaticResources() {
        String filePath = requestPath.replace("/resources", "/resources/static");
        execute(filePath, HttpStatus.OK);
    }

    protected void fileNotFound() {
        logger.warn("File " + requestPath + " not found");
        execute(FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    protected void badRequest() {
        logger.warn("Bad Request");
        execute(BAD_REQUEST, HttpStatus.BAD_REQUEST);
    }

    protected void methodNotImplemented() {
        logger.warn("501 not implemented :" + requestPath + "method");
        execute(METHOD_NOT_SUPPORTED, HttpStatus.NOT_IMPLEMENTED);
    }

    protected void returnIndexFile() {
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
}
