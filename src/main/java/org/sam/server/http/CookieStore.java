package org.sam.server.http;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/07/21
 * Time: 8:35 PM
 */
public class CookieStore {

    List<Cookie> cookies = new ArrayList<>();

    public List<Cookie> parseCookie(String cookies) {
        String[] cookiePairs = cookies.split("; ");
        for (String cookiePairStr : cookiePairs) {
            String[] cookiePair = cookiePairStr.split("=");
            String name = cookiePair[0];
            String value = cookiePair[1];
            this.cookies.add(new Cookie(name, value));
        }

        return this.cookies;
    }
}
