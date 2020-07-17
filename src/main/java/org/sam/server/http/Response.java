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
    private static final String FILE_NOT_FOUND = "static/404.html";
    private static final String METHOD_NOT_SUPPORTED = "static/not_supported";

    private final ClassLoader classLoader = getClass().getClassLoader();

    private final PrintWriter out;
    private final BufferedOutputStream dataOut;
    private final Map<String, Object> headers = new HashMap<>();

    private String returnPath;
    private HttpStatus httpStatus;
    private String contentMimeType;

    public Response(PrintWriter out, BufferedOutputStream dataOut, String path) {
        this.out = out;
        this.dataOut = dataOut;
        this.returnPath = path;
    }

    public static Response create(PrintWriter out, BufferedOutputStream dataOut, String path) {
        return new Response(out, dataOut, path);
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fis = new FileInputStream(file);
        fis.read(fileData);
        return fileData;
    }

    private void printHeader() {
        out.println("HTTP/1.1 " + httpStatus.getCode() + " " + httpStatus.getMessage());
        headers.keySet().forEach(key -> out.println(key + ": " + headers.get(key)));
    }

    private void returnFile(String filePath, HttpStatus status) throws IOException {
        URL fileUrl = classLoader.getResource(filePath);
        if (fileUrl == null) {
            fileNotFound();
            return;
        }

        File file = new File(fileUrl.getFile());
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);

        httpStatus = status;

        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());

        if (!getContentMimeType().equals(ContentType.JSON.getValue())) {
            dataOut.write(fileData, 0, fileLength);
        } else {
            fileLength = 0;
        }
        headers.put("Content-length", fileLength);

        printHeader();
        out.println();
        out.flush();
        dataOut.flush();
    }

    public void fileNotFound() {
        if (HttpServer.verbose) {
            System.out.println("File " + returnPath + " not found");
        }
        try {
            returnFile(FILE_NOT_FOUND, HttpStatus.NOT_FOUND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void methodNotImplemented() throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + returnPath + "method");
        }

        returnFile(METHOD_NOT_SUPPORTED, HttpStatus.NOT_IMPLEMENTED);
    }

    public void returnIndexFile() throws IOException {
        if (this.returnPath.endsWith("/")) {
            returnPath = DEFAULT_FILE;
        }

        returnFile(returnPath, HttpStatus.OK);
    }

    public void setContentMimeType(ContentType contentMimeType) {
        this.contentMimeType = contentMimeType.getValue();
    }

    public String getContentMimeType() {
        if (contentMimeType != null) return contentMimeType;
        if (httpStatus.equals(HttpStatus.NOT_FOUND) || httpStatus.equals(HttpStatus.NOT_IMPLEMENTED)) return "text/html";
        if (this.returnPath.endsWith(".html")) return "text/html";
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
