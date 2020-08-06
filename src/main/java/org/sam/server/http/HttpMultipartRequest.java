package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 4:45 PM
 */
public class HttpMultipartRequest extends HttpRequest {

    private Map<String, Object> files;

    protected HttpMultipartRequest(
            String protocol, String path, HttpMethod method, Map<String, String> headers,
            Map<String, String> parameterMap, Map<String, String> attributes,
            String json, Set<Cookie> cookies, Map<String, Object> files) {
        super(protocol, path, method, headers, parameterMap, attributes, json, cookies);
        this.files = files;
    }

    public MultipartFile getMultipartFile(String name) throws IllegalAccessException {
        Object obj = files.get(name);
        if (obj == null) return null;
        if (MultipartFile.class.equals(obj.getClass())) return (MultipartFile) obj;

        throw new IllegalAccessException("file size is not one.");
    }

    @SuppressWarnings("unchecked")
    public List<MultipartFile> getMultipartFileList(String name) {
        Object obj = files.get(name);
        if (obj == null) return null;
        if (MultipartFile.class.equals(obj.getClass())) return Collections.singletonList((MultipartFile) obj);

        return (ArrayList<MultipartFile>) files.get(name);
    }
}
