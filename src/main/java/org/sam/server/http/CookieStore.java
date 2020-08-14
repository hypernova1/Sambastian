package org.sam.server.http;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/21
 * Time: 8:35 PM
 */
public class CookieStore {

    private static Set<Cookie> cookies = new HashSet<>();

    static Set<Cookie> getCookies() {
        return cookies;
    }

    static Set<Cookie> parseCookie(String cookieStr) {
        String[] cookiePairs = cookieStr.split("; ");
        for (String cookiePairStr : cookiePairs) {
            String[] cookiePair = cookiePairStr.split("=");
            String name = cookiePair[0];
            String value = cookiePair[1];
            cookies.add(new Cookie(name, value));
        }

        return cookies;
    }

    static void vacateList() {
        cookies = new HashSet<>();
    }


}
