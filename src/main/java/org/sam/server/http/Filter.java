package org.sam.server.http;

/**
 * Created by melchor
 * Date: 2020/08/11
 * Time: 5:22 PM
 */
public interface Filter {

    void init();

    void doFilter(Request request, Response response);

    void destroy();

}
