package org.sam.server.http;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * 세션을 관리하는 클래스. 세션의 생명주기를 관리한다.
 *
 * @see org.sam.server.http.Session
 * */
public class SessionManager {

    private static final Set<Session> sessionList = new HashSet<>();

    private SessionManager() {}

    /**
     * 세션을 추가한다.
     *
     * @param session 추가할 세션
     * */
    public static void addSession(Session session) {
        sessionList.add(session);
    }

    /**
     * 세션을 반환한다.
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
     * 세션을 삭제한다.
     *
     * @param id 삭제할 세션의 아이디
     * */
    public static void removeSession(String id) {
        sessionList.removeIf(session -> session.getId().equals(id));
    }

    /**
     * 세션의 만료 시간을 확인 후 만료된 세션을 삭제한다.
     * */
    public static void removeExpiredSession() {
        Iterator<Session> iterator = sessionList.iterator();
        while (iterator.hasNext()) {
            Session session = iterator.next();
            if (!session.isExpired()) continue;
            iterator.remove();
        }
    }
}