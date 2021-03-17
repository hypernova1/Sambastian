package org.sam.server.http;

import org.sam.server.http.context.HttpServer;

import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 세션 정보를 담는 클래스입니다.
 *
 * @author hypernova1
 */
public final class Session {

    private final String id;

    private LocalDateTime creationTime;

    private LocalDateTime accessTime;

    private int timeout;

    private final Map<String, Object> attribute = new Hashtable<>();

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = LocalDateTime.now();
        this.accessTime = LocalDateTime.now();
        this.timeout = 30;
        HttpServer.SessionManager.addSession(this);
        CookieStore.addSession(this.id);
    }

    /**
     * 세션을 무효화합니다.
     * */
    public void invalidate() {
        HttpServer.SessionManager.removeSession(this.id);
    }

    /**
     * 세션에 담긴 요소를 반환합니다.
     *
     * @param key 요소 이름
     * @return 요소 값
     * */
    public Object getAttribute(String key) {
        return this.attribute.get(key);
    }

    /**
     * 세션에 요소를 추가합니다.
     *
     * @param key 요소 이름
     * @param value 요소 값
     * */
    public void addAttribute(String key, String value) {
        this.attribute.put(key, value);
    }

    /**
     * 세션의 아이디를 반환합니다.
     *
     * @return 세션 아이디
     * */
    public String getId() {
        return id;
    }

    /**
     * 세션의 생성 시간을 반환합니다.
     *
     * @return 세션 생성 시간
     * */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * 세션의 생성 시간을 설정합니다.
     *
     * @param creationTime 세션 생성 시간
     * */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 마지막으로 세션에 접근한 시간을 반환합니다.
     *
     * @return 최종 세션 접근 시간
     * */
    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    /**
     * 마지막으로 세션에 접근한 시간을 설정합니다.
     *
     * @param accessTime 최종 세션 접근 시간
     * */
    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * 세션의 유효 시간을 가져옵니다.
     *
     * @return 세션 유효 시간
     * */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 세션의 유효 시간을 설정합니다.
     *
     * @param timeout 세션 유효 시간
     * */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 세션에 요소를 추가합니다.
     *
     * @param name 요소 이름
     * @param value 요소 값
     * */
    public void setAttribute(String name, Object value) {
        this.attribute.put(name, value);
    }

    /**
     * 세션 만료 시간을 반환합니다.
     *
     * @return 세션 만료 시간
     * */
    public LocalDateTime getExpired() {
        return this.accessTime.plusMinutes(this.timeout);
    }

    /**
     * 최종 접근 시간을 현재 시간으로 변경합니다.
     * */
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
