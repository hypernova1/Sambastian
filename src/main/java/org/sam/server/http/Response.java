package org.sam.server.http;

import org.sam.server.HttpServer;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;

import java.io.*;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class Response {

    private static final String DEFAULT_FILE = "static/index.html";
    private static final String BAD_REQUEST = "static/400.html";
    private static final String FILE_NOT_FOUND = "static/404.html";
    private static final String METHOD_NOT_SUPPORTED = "static/not_supported";

    private final ClassLoader classLoader = getClass().getClassLoader();

    private final PrintWriter out;
    private final BufferedOutputStream bos;
    private final Map<String, Object> headers = new HashMap<>();
    private final String requestPath;

    private String filePath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    public Response(OutputStream os, String path) {
        this.out = new PrintWriter(os);
        this.bos = new BufferedOutputStream(os);
        this.requestPath = path;
    }

    public static Response create(OutputStream os, String path) {
        return new Response(os, path);
    }

    public void pass(String filePath, HttpStatus status) throws IOException {
        this.httpStatus = status;

        int fileLength = 0;
        if (getContentMimeType().equals(ContentType.JSON.getValue())) {
            fileLength = creteJson(filePath);
        } else {
            fileLength = createStaticFile(filePath);
        }

        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());
        headers.put("Content-length", fileLength);

        printHeader();

        out.flush();
        bos.flush();
    }

    private int createStaticFile(String filePath) throws IOException {
        URL fileUrl = classLoader.getResource(filePath);

        if (fileUrl == null) {
            fileNotFound();
            return 0;
        }

        File file = new File(fileUrl.getFile());
        int fileLength = (int) file.length();
        byte[] fileData = readFile(file, fileLength);
        bos.write(fileData, 0, fileLength);

        return fileLength;
    }

    private int creteJson(String filePath) throws IOException {
        if (httpStatus.equals(HttpStatus.NOT_FOUND) ||
                httpStatus.equals(HttpStatus.BAD_REQUEST)) return 0;
        byte[] bytes = filePath.getBytes();
        bos.write(bytes);
        return bytes.length;
    }

    private void printHeader() {
        out.println("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage());
        headers.keySet().forEach(key -> out.println(key + ": " + headers.get(key)));
        out.println();
    }

    private byte[] readFile(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileData);
        return fileData;
    }

    public void fileNotFound() {
        if (HttpServer.verbose) {
            System.out.println("File " + requestPath + " not found");
        }
        try {
            pass(FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void badRequest() {
        if (HttpServer.verbose) {
            System.out.println("Bad Request");
        }
        try {
            pass(BAD_REQUEST, HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void methodNotImplemented() throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + requestPath + "method");
        }

        pass(METHOD_NOT_SUPPORTED, HttpStatus.NOT_IMPLEMENTED);
    }

    public void returnIndexFile() throws IOException {
        if (this.requestPath.endsWith("/")) {
            filePath = DEFAULT_FILE;
        }

        pass(filePath, HttpStatus.OK);
    }

    public void setContentMimeType(ContentType contentMimeType) {
        this.contentMimeType = contentMimeType.getValue();
    }

    public String getContentMimeType() {
        if (contentMimeType != null) return contentMimeType;
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.BAD_REQUEST) || httpStatus.equals(HttpStatus.NOT_IMPLEMENTED)) return "text/html";
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
