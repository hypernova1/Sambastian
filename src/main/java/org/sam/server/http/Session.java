package org.sam.server.http;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 정보를 담는 클래스
 *
 * @author hypernova1
 */
public final class Session {

    private final String id;
    private LocalDateTime creationTime;
    private LocalDateTime accessTime;
    private int timeout;

    private final Map<String, Object> attribute = new ConcurrentHashMap<>();

    public Session() {
        this.id = UUID.randomUUID().toString();
        this.creationTime = LocalDateTime.now();
        this.accessTime = LocalDateTime.now();
        this.timeout = 30;
        SessionManager.addSession(this);
        CookieStore.addSession(this.id);
    }

    /**
     * 세션을 무효화한다.
     * */
    public void invalidate() {
        SessionManager.removeSession(this.id);
    }

    /**
     * 세션에 담긴 요소를 반환한다.
     *
     * @param key 요소 이름
     * @return 요소 값
     * */
    public Object getAttribute(String key) {
        return this.attribute.get(key);
    }

    /**
     * 세션에 요소를 추가한다.
     *
     * @param key 요소 이름
     * @param value 요소 값
     * */
    public void addAttribute(String key, String value) {
        this.attribute.put(key, value);
    }

    /**
     * 세션의 아이디를 반환한다.
     *
     * @return 세션 아이디
     * */
    public String getId() {
        return id;
    }

    /**
     * 세션의 생성 시간을 반환한다.
     *
     * @return 세션 생성 시간
     * */
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    /**
     * 세션의 생성 시간을 설정한다.
     *
     * @param creationTime 세션 생성 시간
     * */
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    /**
     * 마지막으로 세션에 접근한 시간을 반환한다.
     *
     * @return 최종 세션 접근 시간
     * */
    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    /**
     * 마지막으로 세션에 접근한 시간을 설정한다.
     *
     * @param accessTime 최종 세션 접근 시간
     * */
    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    /**
     * 세션의 유효 시간을 가져온다.
     *
     * @return 세션 유효 시간
     * */
    public int getTimeout() {
        return timeout;
    }

    /**
     * 세션의 유효 시간을 설정한다.
     *
     * @param timeout 세션 유효 시간
     * */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 세션에 요소를 추가한다.
     *
     * @param name 요소 이름
     * @param value 요소 값
     * */
    public void setAttribute(String name, Object value) {
        this.attribute.put(name, value);
    }

    /**
     * 세션 만료 시간을 반환한다.
     *
     * @return 세션 만료 시간
     * */
    public LocalDateTime getExpired() {
        return this.accessTime.plusMinutes(this.timeout);
    }

    /**
     * 최종 접근 시간을 현재 시간으로 변경한다.
     * */
    public void renewAccessTime() {
        this.accessTime = LocalDateTime.now();
    }


    /**
     * 만료된 세션인지 확인 한다.
     *
     * @return 만료 여부
     * */
    public boolean isExpired() {
        long accessTime = this.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long now = System.currentTimeMillis();
        int timeout = this.getTimeout() * 1000 * 1800;
        return now - accessTime > timeout;
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
