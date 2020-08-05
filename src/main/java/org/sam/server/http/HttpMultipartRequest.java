package org.sam.server.http;

import org.sam.server.constant.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/22
 * Time: 4:45 PM
 */
public class HttpMultipartRequest extends HttpRequest {

    private Map<String, Object> files;

    protected HttpMultipartRequest(String path, HttpMethod method, Map<String, String> headers,
                                   Map<String, String> parameterMap, Map<String, Object> attributes,
                                   String json, Set<Cookie> cookies, Map<String, Object> files) {
        super(path, method, headers, parameterMap, attributes, json, cookies);
        this.files = files;
    }

    public MultipartFile getMultipartFile(String name) throws IllegalAccessException {
        MultipartFile multipartFile;
        try {
            multipartFile = (MultipartFile) files.get(name);
        } catch (ClassCastException e) {
            throw new IllegalAccessException("파일이 0개 이거나 여러개입니다.");
        }
        return multipartFile;
    }

    public List<MultipartFile> getMultipartFileList(String name) throws IllegalAccessException {
        List<MultipartFile> multipartFiles;
        try {
            multipartFiles = (ArrayList<MultipartFile>) files.get(name);
        } catch (ClassCastException e) {
            throw new IllegalAccessException("파일을 리스트로 받을 수 없습니다.");
        }

        return multipartFiles;
    }
}
