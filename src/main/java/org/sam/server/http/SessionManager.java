package org.sam.server.http;

import java.time.ZoneId;
import java.util.List;
import java.util.TimerTask;

/**
 * Created by melchor
 * Date: 2020/07/24
 * Time: 8:54 PM
 */
public class SessionManager extends TimerTask {

    private List<Session> sessionList;

    public Session createSession() {
        Session session = new Session();
        sessionList.add(session);
        return session;
    }

    public Session getSession(String id) {
        for (Session session : sessionList) {
            if (session.getId().equals(id)) {
                return session;
            }
        }
        return null;
    }

    @Override
    public void run() {
        sessionList.forEach(session -> {
            long accessTime = session.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long now = System.currentTimeMillis();
            int timeout = session.getTimeout() * 1000;
            if (timeout > now - accessTime) {
                sessionList.remove(session);
            }
        });
    }
}
