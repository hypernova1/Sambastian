package org.sam.server.http;

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Created by melchor
 * Date: 2020/07/30
 * Time: 9:20 AM
 */
public abstract class Response {

    protected static final String DEFAULT_FILE = "static/index.html";
    protected static final String BAD_REQUEST = "static/400.html";
    protected static final String FILE_NOT_FOUND = "static/404.html";
    protected static final String FAVICON = "favicon.ico";
    protected static final String METHOD_NOT_SUPPORTED = "static/not_supported.html";

    protected final PrintWriter writer;
    protected final BufferedOutputStream outputStream;

    protected Response(OutputStream os) {
        this.writer = new PrintWriter(os);
        this.outputStream = new BufferedOutputStream(os);
    }

}
