package org.sam.server.http;

import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.util.*;

/**
 * Created by melchor
 * Date: 2020/07/24
 * Time: 8:54 PM
 */
public class SessionManager extends TimerTask {

    private static final Logger logger = Logger.getLogger(SessionManager.class);

    private final Set<Session> sessionList = new HashSet<>();

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

    public void removeSession(String id) {
        sessionList.removeIf(session -> session.getId().equals(id));
    }

    @Override
    public void run() {
        Iterator<Session> iterator = sessionList.iterator();
        while (iterator.hasNext()) {
            Session session = iterator.next();
            long accessTime = session.getAccessTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            long now = System.currentTimeMillis();
            int timeout = session.getTimeout() * 1000;
            if (now - accessTime > timeout) {
                iterator.remove();
                logger.info("remove Session:" + session.getId());
            }
        }
    }
}
