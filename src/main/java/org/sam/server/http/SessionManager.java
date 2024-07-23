package org.sam.server.http;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션을 관리하는 클래스. 세션의 생명주기를 관리한다.
 *
 * @see org.sam.server.http.Session
 * */
public class SessionManager {

    private static final Map<String, Session> sessionMap = new ConcurrentHashMap<>();

    private SessionManager() {}

    /**
     * 세션을 추가한다.
     *
     * @param session 추가할 세션
     * */
    public static void addSession(Session session) {
        sessionMap.put(session.getId(), session);
    }

    /**
     * 세션을 반환한다.
     *
     * @param id 가져올 세션의 아이디
     * @return 세션
     * */
    public static Session getSession(String id) {
        Session session = sessionMap.get(id);
        if (session.isExpired()) {
            sessionMap.remove(id);
            return null;
        }
        return session;
    }

    /**
     * 세션을 삭제한다.
     *
     * @param id 삭제할 세션의 아이디
     * */
    public static void removeSession(String id) {
        sessionMap.remove(id);
    }

    /**
     * 세션의 만료 시간을 확인 후 만료된 세션을 삭제한다.
     * */
    public static void removeExpiredSession() {
        Set<Map.Entry<String, Session>> entrySet = sessionMap.entrySet();
        Iterator<Map.Entry<String, Session>> iterator = entrySet.iterator();

        while (iterator.hasNext()) {
            Session session = iterator.next().getValue();
            if (!session.isExpired()) {
                continue;
            }
            iterator.remove();
        }
    }
}