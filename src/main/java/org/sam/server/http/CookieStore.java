package org.sam.server.http;

import java.util.HashSet;
import java.util.Set;

/**
 * 쿠키를 관리하는 클래스입니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.Cookie
 */
public class CookieStore {

    private static final Set<Cookie> cookies = new HashSet<>();

    /**
     * 쿠키의 목록을 반환합니다.
     * 
     * @return 쿠키 목록
     * */
    public static Set<Cookie> getCookies() {
        return cookies;
    }

    /**
     * HTTP 요청 헤더에서 쿠키 부분을 읽어 파싱합니다.
     * 
     * @param cookieStr 쿠키 내용
     * @return 쿠키 목록
     * */
    public static Set<Cookie> parseCookie(String cookieStr) {
        String[] cookiePairs = cookieStr.split("; ");
        for (String cookiePairStr : cookiePairs) {
            String[] cookiePair = cookiePairStr.split("=");
            String name = cookiePair[0];
            String value = cookiePair[1];
            cookies.add(new Cookie(name, value));
        }

        return cookies;
    }

    /**
     * 쿠키 목록을 초기화 합니다.
     * */
    public static void vacateList() {
        cookies.clear();
    }


    /**
     * 세션 정보를 추가합니다.
     * */
    public static void addSession(String id) {
        Cookie cookie = new Cookie("sessionId", id);
        CookieStore.getCookies().add(cookie);
    }
}
