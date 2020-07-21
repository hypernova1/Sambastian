package org.sam.server.http;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by melchor
 * Date: 2020/07/21
 * Time: 8:34 PM
 */
public class Cookie {
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

    public String getAfterTime(int minutes) {
        Date expiredDate = new Date();
        expiredDate.setTime(expiredDate.getTime() + (1000 * minutes));
        DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(expiredDate);
    }

    public String getExpires() {
        return this.expires;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setMaxAge(int minutes) {
        this.maxAge = minutes;
        this.expires = getAfterTime(minutes);
    }

    public int getMaxAge() {
        return this.maxAge;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
