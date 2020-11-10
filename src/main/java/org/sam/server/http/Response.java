package org.sam.server.http;

import org.sam.server.common.ServerProperties;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *  요청을 해석하고 응답하는 클래스입니다.
 *
 * @author hypernova1
 * @see HttpResponse
 * */
public abstract class Response {

    protected static final String DEFAULT_FILE = "static/index.html";
    protected static final String BAD_REQUEST = "static/400.html";
    protected static final String NOT_FOUND = "static/404.html";
    protected static final String FAVICON = "favicon.ico";
    protected static final String METHOD_NOT_ALLOWED = "static/method_not_allowed.html";

    protected final PrintWriter writer;
    protected final BufferedOutputStream outputStream;

    protected Response(OutputStream os) {
        String bufferSizeStr = ServerProperties.get("file-buffer-size");
        int bufferSize = bufferSizeStr != null ? Integer.parseInt(bufferSizeStr) : 8192;
        this.writer = new PrintWriter(os);
        this.outputStream = new BufferedOutputStream(os, bufferSize);
    }

}
