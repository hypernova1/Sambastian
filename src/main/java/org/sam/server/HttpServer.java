package org.sam.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpServer implements Runnable {

    private static final File WEB_ROOT = new File(".");
    private static final String DEFAULT_FILE = "index.html";
    private static final String FILE_NOT_FOUND = "404.html";
    private static final String METHOD_NOT_SUPPORTED = "not_supported";
    private static final int PORT = 8080;

    static final boolean verbose = true;
    private final Socket connect;

    public HttpServer(Socket connect) {
        this.connect = connect;
    }

    public static void main(String[] args) {
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("server started..");

            while (true) {
                HttpServer httpServer = new HttpServer(serverSocket.accept());

                if (verbose) {
                    System.out.println("connected.." + new Date());
                }
                Thread thread = new Thread(httpServer);
                thread.start();
                System.out.println("Thread Count: " + Thread.activeCount());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        String fileRequested = null;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
             PrintWriter out = new PrintWriter(connect.getOutputStream());
             BufferedOutputStream dataOut = new BufferedOutputStream(connect.getOutputStream())) {

            String input = in.readLine();
            StringTokenizer parse = new StringTokenizer(input);

            String method = parse.nextToken().toUpperCase();
            fileRequested = parse.nextToken().toLowerCase();

            if (!method.equals("GET") && !method.equals("HEAD")) {
                if (!verbose) {
                    System.out.println("501 not implemented :" + method + "method");
                }

                File file = new File(WEB_ROOT, METHOD_NOT_SUPPORTED);
                int fileLength = (int) file.length();
                String contentMimeType = "text/html";

                byte[] fileData = readFileData(file, fileLength);

                out.println("HTTP/1.1 501 Not Implemented");
                getDefaultResponseHeader(out, fileLength, contentMimeType);

                out.println();
                out.flush();
                dataOut.write(fileData, 0, fileLength);
                dataOut.close();
            } else {
                if (fileRequested.endsWith("/")) {
                    fileRequested += DEFAULT_FILE;
                }

                File file = new File(WEB_ROOT, fileRequested);
                int fileLength = (int) file.length();
                String contentType = getContentType(fileRequested);

                if (method.equals("GET")) {
                    byte[] fileData = null;
                    try {
                        fileData = readFileData(file, fileLength);

                    } catch(FileNotFoundException e) {
                        fileNotFound(out, dataOut, fileRequested);
                        throw new RuntimeException(e);
                    }

                    out.println("HTTP/1.1 200 OK");
                    getDefaultResponseHeader(out, fileLength, contentType);

                    out.println();
                    out.flush();
                    dataOut.write(fileData, 0, fileLength);
                    dataOut.flush();
                    connect.close();
                }

                if (verbose) {
                    System.out.println("File " + fileRequested + " of type " + contentType + " returned");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] readFileData(File file, int fileLength) throws IOException {
        byte[] fileData = new byte[fileLength];
        FileInputStream fileIn = new FileInputStream(file);
        fileIn.read(fileData);
        return fileData;
    }

    private String getContentType(String fileRequested) {
        if (fileRequested.endsWith(".html")) return "text/html";
        return "text/plain";
    }

    private void fileNotFound(PrintWriter out, OutputStream dataOut, String fileRequested) throws IOException {
        File file = new File(WEB_ROOT, FILE_NOT_FOUND);
        int fileLength = (int) file.length();
        String content = "text/html";
        byte[] fileData = readFileData(file, fileLength);

        out.println("HTTP/1.1 404 File Not Found");
        getDefaultResponseHeader(out, fileLength, content);
        out.println();
        out.flush();

        dataOut.write(fileData, 0, fileLength);
        dataOut.flush();

        if (verbose) {
            System.out.println("File " + fileRequested + " not found");
        }
    }

    private void getDefaultResponseHeader(PrintWriter out, int fileLength, String contentMimeType) {
        out.println("Server: Java HTTP Server from sam : 1.0");
        out.println("Date: " + new Date());
        out.println("Content-Type: " + contentMimeType);
        out.println("Content-length" + fileLength);
    }
}
