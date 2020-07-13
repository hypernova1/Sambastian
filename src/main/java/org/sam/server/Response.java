package org.sam.server;

import java.io.*;
import java.util.Date;

public class Response {

    private PrintWriter out;
    private BufferedOutputStream dataOut;
    private static final File WEB_ROOT = new File(".");
    private String path;

    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported";

    public Response(PrintWriter out, BufferedOutputStream dataOut, String path) {
        this.out = out;
        this.dataOut = dataOut;
        this.path = path;
    }

    public static Response create(PrintWriter out, BufferedOutputStream dataOut, String path) {
        return new Response(out, dataOut, path);
    }

    public void execute() throws IOException {
        byte[] fileData;
        File file = new File(WEB_ROOT, path);
        int fileLength = (int) file.length();

        fileData = readFileData(file, fileLength);

        String status = "200 OK";
        getDefaultResponseHeader(fileLength, status, getContentMimeType());
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = new FileInputStream(file);
        fileIn.read(fileData);
        return fileData;
    }

    private void getDefaultResponseHeader(int fileLength, String contentMimeType, String status) {
        out.println("HTTP/1.1 " + status);
        out.println("Server: Java HTTP Server from sam : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentMimeType);
        out.println("Content-length: " + fileLength);

    }

    void notImplemented(Request request) throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + request.getMethod() + "method");
        }

        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();

        byte[] fileData = readFileData(file, fileLength);

        String status = "501 Not Implemented";
        String contentMimeType = "text/html";
        getDefaultResponseHeader(fileLength, status, contentMimeType);
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();
    }

    public void fileNotFound(Request request) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);

        String status = "404 File Not Found";
        String contentMimeType = "text/html";
        getDefaultResponseHeader(fileLength, status, contentMimeType);
        dataOut.write(fileData, 0, fileLength);

        out.println();
        out.flush();
        dataOut.flush();

        if (HttpServer.verbose) {
            System.out.println("File " + request.getPath() + " not found");
        }
    }

    public String getContentMimeType() {
        if (this.path.endsWith(".html")) return "text/html";
        return "text/plain";
    }

}
