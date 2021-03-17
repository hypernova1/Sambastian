package org.sam.server.http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * HTTP 쿠키 클래스입니다.
 *
 * @author hypernova1
 * @see org.sam.server.http.CookieStore
 */
public class Cookie {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.US);

    private String name;

    private String value;

    private String expires;

    private int maxAge;

    private String domain;

    private String path;

    private boolean httpOnly;

    public Cookie(String name, String value) {
        this.name = name;
        this.value = value;
        this.path = "/";
    }

    /**
     * 쿠키의 유효 날짜 및 시간을 반환합니다.
     *
     * @param minutes 유효 시간
     * @return 쿠키의 유효 날짜 및 시간
     * */
    public String getAfterTime(int minutes) {
        Date expiredDate = new Date();
        expiredDate.setTime(expiredDate.getTime() + (1000L * minutes));
        DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
        return DATE_FORMAT.format(expiredDate);
    }

    /**
     * 쿠키의 만료 시간 반환합니다.
     * 
     * @return 쿠키 만료 시간
     * */
    public String getExpires() {
        return this.expires;
    }

    /**
     * 쿠키의 이름을 반환합니다.
     *
     * @return 쿠키 이름
     * */
    public String getName() {
        return name;
    }

    /**
     * 쿠키의 이름을 설정합니다.
     * 
     * @param name 쿠키 이름
    * */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 쿠키의 값을 반환합니다.
     * 
     * @return 쿠키 값
     * */
    public String getValue() {
        return value;
    }

    /**
     * 쿠키의 값을 설정합니다.
     * 
     * @param value 쿠키 값
     * */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * 쿠키의 Max-Age을 설정합니다.
     *
     * @param minutes 유효 시간
     * */
    public void setMaxAge(int minutes) {
        this.maxAge = minutes;
        this.expires = getAfterTime(minutes);
    }

    /**
     * 쿠키의 유효 시간을 반환합니다.
     * 
     * @return 유효 시간
     * */
    public int getMaxAge() {
        return this.maxAge;
    }

    /**
     * 쿠키가 적용되어야 하는 호스트를 반환합니다.
     * 
     * @return 호스트명
     * */
    public String getDomain() {
        return domain;
    }

    /**
     * 쿠키가 적용되어야 하는 호스트를 설정합니다.
     * 
     * @param domain 도메인명
     * */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * HTTP-only 유무를 반환합니다.
     *
     * @return HTTP-only 유무
     * */
    public boolean isHttpOnly() {
        return httpOnly;
    }

    /**
     * HTTP-only 유무를 설정합니다
     * 
     * @param httpOnly 자바스크립트에서 허용 유무
     * */
    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    /**
     * Path를 반환합니다
     *
     * @return 쿠키의 범위
     * */
    public String getPath() {
        return path;
    }

    /**
     * path를 설정합니다
     * 
     * @param path 쿠키의 범위
     * */
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cookie))
            return false;
        Cookie cookie = (Cookie) o;
        return this.name.equals(cookie.name);
    }
}
