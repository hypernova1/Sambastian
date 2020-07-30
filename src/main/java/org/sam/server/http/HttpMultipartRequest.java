package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.io.File;
import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 4:45 PM
 */
public class HttpMultipartRequest extends HttpRequest {

    private Map<String, MultipartFile> files;

    protected HttpMultipartRequest(String path, HttpMethod method, Map<String, String> headers,
                                   Map<String, String> parameterMap, Map<String, Object> attributes,
                                   String json, Set<Cookie> cookies, Map<String, MultipartFile> files) {
        super(path, method, headers, parameterMap, attributes, json, cookies);
        this.files = files;
    }

    public MultipartFile getFile(String name) {
        return files.get(name);
    }
}