package org.sam.server;

import java.io.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.sam.server.constant.HttpStatus;

public class Response {

    private static final File WEB_ROOT = new File(".");
    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported";

    private PrintWriter out;
    private BufferedOutputStream dataOut;
    private String path;
    private Map<String, Object> headers = new HashMap<>();

    private HttpStatus httpStatus;

    public Response(PrintWriter out, BufferedOutputStream dataOut, String path) {
        this.out = out;
        this.dataOut = dataOut;
        this.path = path;
    }

    public static Response create(PrintWriter out, BufferedOutputStream dataOut, String path) {
        return new Response(out, dataOut, path);
    }

    public void execute() throws IOException {
        if (this.path.endsWith("/")) {
            path += DEFAULT_FILE;
        }
        byte[] fileData;
        File file = new File(WEB_ROOT, path);
        int fileLength = (int) file.length();

        fileData = readFileData(file, fileLength);
        httpStatus = HttpStatus.OK;
        headers.put("Server", "Java HTTP Server from sam : 1.0");
        headers.put("Date", LocalDateTime.now());
        headers.put("Content-Type", getContentMimeType());
        headers.put("Content-length", fileLength);

        printHeader();
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();
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

    void notImplemented(Request request) throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + request.getMethod() + "method");
        }

        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();

        byte[] fileData = readFileData(file, fileLength);

        httpStatus = HttpStatus.NOT_IMPLEMENTED;

        printHeader();
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();
    }

    public void fileNotFound(Request request) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);

        httpStatus = HttpStatus.NOT_FOUND;
        printHeader();
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();

        if (HttpServer.verbose) {
            System.out.println("File " + request.getPath() + " not found");
        }
    }

    private void setHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public String getContentMimeType() {
        if (httpStatus.getCode().equals("404") || httpStatus.getCode().equals("NOT_IMPLEMENTED")) return "text/html";
        if (this.path.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

    public Object getHeader(String key) {
        return headers.get(key);
    }

}
