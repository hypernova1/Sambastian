package org.sam.server.http.handler;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 경로를 읽어 핸들러를 찾는 클래스
 * */
public class HandlerPathMatcher {

    private static final Pattern PATH_VALUE_PATTERN = Pattern.compile("[{](.*?)[}]");

    public static boolean containsUrlParameter(String url) {
        return url.contains("{") && url.contains("}");
    }

    public static boolean isMatchedPath(String requestPath, String path, Queue<String> paramNames, Map<String, String> parameters) {
        if (!containsUrlParameter(path)) return false;
        String[] requestPathArr = requestPath.split("/");
        String[] pathArr = path.split("/");
        if (requestPathArr.length != pathArr.length) {
            return false;
        }
        for (int i = 0; i < pathArr.length; i++) {
            if (containsUrlParameter(pathArr[i])) {
                parameters.put(paramNames.poll(), requestPathArr[i]);
                continue;
            }
            if (!pathArr[i].equals(requestPathArr[i])) {
                return false;
            }
        }
        return true;
    }

    public static Queue<String> extractPathVariables(String path) {
        Matcher matcher = PATH_VALUE_PATTERN.matcher(path);
        Queue<String> paramNames = new ArrayDeque<>();
        while (matcher.find()) {
            paramNames.add(matcher.group(1));
        }
        return paramNames;
    }
}