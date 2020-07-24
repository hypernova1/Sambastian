package org.sam.server.http;

import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by melchor
 * Date: 2020/07/24
 * Time: 8:40 PM
 */
public class Session {

    private String id;
    private LocalDateTime creationTime;
    private LocalDateTime accessTime;
    private int timeout;

    private Map<String, Object> attribute = new Hashtable<>();

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = LocalDateTime.now();
        this.accessTime = LocalDateTime.now();
        this.timeout = 30;

        Cookie cookie = new Cookie("sessionId", this.id);
        CookieStore.getCookies().add(cookie);
    }

    public Object getAttribute(String key) {
        return this.attribute.get(key);
    }

    public void addAttribute(String key, String value) {
        this.attribute.put(key, value);
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    public int getTimeout() {
        return timeout;
    }

    public Map<String, Object> getAttribute() {
        return attribute;
    }

    public void setAttribute(Map<String, Object> attribute) {
        this.attribute = attribute;
    }

    public LocalDateTime getExpired() {
        return this.accessTime.plusMinutes(this.timeout);
    }

    public void renewAccessTime() {
        this.accessTime = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return id.equals(session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
