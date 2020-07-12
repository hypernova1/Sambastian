package org.sam.server;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class Handler {

    private static final File WEB_ROOT = new File(".");
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported";

    public static final boolean verbose = true;
    private static final String DEFAULT_FILE = "index.html";

    private final Socket connect;


    public Handler(Socket connect) {
        this.connect = connect;
    }

    public void analyze(BufferedReader in, PrintWriter out, BufferedOutputStream dataOut) throws IOException {

        String input = in.readLine();
        StringTokenizer parse = new StringTokenizer(input);

        String method = parse.nextToken().toUpperCase();
        String fileRequested = parse.nextToken().toLowerCase();
        String contentType = this.getContentType(fileRequested);

        if (Handler.verbose) {
            System.out.println("File " + fileRequested + " of type " + contentType + " returned");
        }

        if (!method.equals("GET") && !method.equals("HEAD")) {
            this.notImplemented(out, dataOut, method);
            return;
        }

        if (fileRequested.endsWith("/")) {
            fileRequested += DEFAULT_FILE;
        }

        File file = new File(WEB_ROOT, fileRequested);

        if (method.equals("GET")) {
            try {
                this.doGet(out, dataOut, file, contentType);
            } catch (FileNotFoundException e) {
                this.fileNotFound(out, dataOut, fileRequested);
                throw new RuntimeException(e);
            }
        }
    }

    public void doGet(PrintWriter out, BufferedOutputStream dataOut, File file, String contentType) throws IOException {
        byte[] fileData = null;
        int fileLength = (int) file.length();
        fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 200 OK");
        getDefaultResponseHeader(out, fileLength, contentType);

        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();
        connect.close();
    }

    void getDefaultResponseHeader(PrintWriter out, int fileLength, String contentMimeType) {
        out.println("Server: Java HTTP Server from sam : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentMimeType);
        out.println("Content-length: " + fileLength);
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = new FileInputStream(file);
        fileIn.read(fileData);
        return fileData;
    }

    void notImplemented(PrintWriter out, BufferedOutputStream dataOut, String method) throws IOException {
        if (!verbose) {
            System.out.println("501 not implemented :" + method + "method");
        }

        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();
        String contentMimeType = "text/html";

        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 501 Not Implemented");
        this.getDefaultResponseHeader(out, fileLength, contentMimeType);

        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.close();
    }

    public void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        this.getDefaultResponseHeader(out, fileLength, content);

        out.println();
        out.flush();
        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        return "text/plain";
    }
}
