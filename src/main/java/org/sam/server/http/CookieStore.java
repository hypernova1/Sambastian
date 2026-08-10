package org.sam.server.http;

import java.util.HashSet;
import java.util.Set;

/**
 * HTTP 요청의 Cookie 헤더를 파싱하는 유틸 클래스.
 * 쿠키는 요청/응답 인스턴스가 소유하며 전역 상태를 갖지 않는다.
 *
 * @author hypernova1
 * @see org.sam.server.http.Cookie
 */
public class CookieStore {

    private CookieStore() {}

    /**
     * HTTP 요청 헤더에서 쿠키 부분을 읽어 파싱한다.
     *
     * @param cookieStr 쿠키 내용
     * @return 쿠키 목록
     * */
    public static Set<Cookie> parseCookie(String cookieStr) {
        Set<Cookie> cookies = new HashSet<>();
        if (cookieStr == null) {
            return cookies;
        }
        String[] cookiePairs = cookieStr.split(";");
        for (String cookiePairStr : cookiePairs) {
            String trimmed = cookiePairStr.trim();
            int separatorIndex = trimmed.indexOf('=');
            if (separatorIndex <= 0) {
                continue;
            }
            String name = trimmed.substring(0, separatorIndex);
            String value = trimmed.substring(separatorIndex + 1);
            cookies.add(new Cookie(name, value));
        }

        return cookies;
    }
}
