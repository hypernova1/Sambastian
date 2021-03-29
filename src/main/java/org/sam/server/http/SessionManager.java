package org.sam.server.http;

import org.sam.server.http.web.Request;

import java.time.ZoneId;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 세션을 관리하는 클래스입니다. 세션의 생명주기를 관리합니다.
 *
 * @see org.sam.server.http.Session
 * */
public class SessionManager {

    private static final Set<Session> sessionList = new HashSet<>();

    private SessionManager() {}

    /**
     * 세션을 추가합니다.
     *
     * @param session 추가할 세션
     * */
    public static void addSession(Session session) {
        sessionList.add(session);
    }

    /**
     * 세션을 반환합니다.
     *
     * @param id 가져올 세션의 아이디
     * @return 세션
     * */
    public static Session getSession(String id) {
        return sessionList.stream()
                .filter(session -> session.getId().equals(id))
                .findFirst().orElse(null);
    }

    /**
     * 세션을 삭제합니다.
     *
     * @param id 삭제할 세션의 아이디
     * */
    public static void removeSession(String id) {
        sessionList.removeIf(session -> session.getId().equals(id));
    }

    /**
     * 세션의 만료 시간을 확인 후 만료된 세션을 삭제합니다.
     * */
    public static void removeExpiredSession() {
        Iterator<Session> iterator = sessionList.iterator();
        while (iterator.hasNext()) {
            Session session = iterator.next();
            if (!isExpiredSession(session)) continue;
            iterator.remove();
        }
    }

    /**
     * 만료된 세션인지 확인 합니다.
     *
     * @param session 세션
     * @return 만료 여부
     * */
    private static boolean isExpiredSession(Session session) {
        long accessTime = session.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long now = System.currentTimeMillis();
        int timeout = session.getTimeout() * 1000 * 1800;
        return now - accessTime > timeout;
    }

    /**
     * 핸들러 실행시 필요한 세션을 가져옵니다.
     *
     * @param request 요청 인스턴스
     * @return 세션
     * */
    public static Session getSessionFromRequest(Request request) {
        Set<Cookie> cookies = request.getCookies();
        Iterator<Cookie> iterator = cookies.iterator();
        while (iterator.hasNext()) {
            Cookie cookie = iterator.next();
            if (!cookie.getName().equals("sessionId")) continue;

            Session session = request.getSession();
            if (session != null) {
                session.renewAccessTime();
                return session;
            }
            iterator.remove();
        }
        return new Session();
    }
}