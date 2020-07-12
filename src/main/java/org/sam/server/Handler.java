package org.sam.server;

import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class Handler {

    private static final File WEB_ROOT = new File(".");
    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported";

    private Socket connect;

    private BufferedReader in;
    private PrintWriter out;
    private BufferedOutputStream dataOut;

    private String method;
    private String requestPath;
    private String contentType;

    public Handler(Socket connect) {
        this.connect = connect;
        try {
            this.in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
            this.out = new PrintWriter(connect.getOutputStream());
            this.dataOut = new BufferedOutputStream(connect.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestAnalyze() {
        try {
            String input = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);

            method = parse.nextToken().toUpperCase();
            requestPath = parse.nextToken().toLowerCase();

            if (requestPath.endsWith("/")) {
                requestPath += DEFAULT_FILE;
            }

            contentType = getContentType();

            if (HttpServer.verbose) {
                System.out.println("File " + requestPath + " of type " + contentType + " returned");
            }

            if (!method.equals("GET") && !method.equals("HEAD")) {
                notImplemented();
                return;
            }

            File file = new File(WEB_ROOT, requestPath);

            if (method.equals("GET")) {
                try {
                    doGet(file);
                } catch (FileNotFoundException e) {
                    fileNotFound();
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                closeResource();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public void doGet(File file) throws IOException {
        byte[] fileData;
        int fileLength = (int) file.length();
        fileData = readFileData(file, fileLength);

        String status = "200 OK";
        getDefaultResponseHeader(fileLength, status, contentType);
        dataOut.write(fileData, 0, fileLength);
    }

    void notImplemented() throws IOException {
        if (!HttpServer.verbose) {
            System.out.println("501 not implemented :" + method + "method");
        }

        File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
        int fileLength = (int) file.length();

        byte[] fileData = readFileData(file, fileLength);

        String status = "501 Not Implemented";
        String contentMimeType = "text/html";
        getDefaultResponseHeader(fileLength, status, contentMimeType);
        dataOut.write(fileData, 0, fileLength);
    }

    public void fileNotFound() throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        byte[] fileData = readFileData(file, fileLength);

        String status = "404 File Not Found";
        String contentMimeType = "text/html";
        getDefaultResponseHeader(fileLength, status, contentMimeType);
        dataOut.write(fileData, 0, fileLength);

        if (HttpServer.verbose) {
            System.out.println("File " + requestPath + " not found");
        }
    }

    private String getContentType() {
        if (requestPath.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    private void closeResource() throws IOException {
        out.println();
        out.flush();
        dataOut.flush();
        connect.close();
    }

    private void getDefaultResponseHeader(int fileLength, String contentMimeType, String status) {
        out.println("HTTP/1.1 " + status);
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
}
