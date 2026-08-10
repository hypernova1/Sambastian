package org.sam.server.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionTest {

    @Test
    @DisplayName("접근 시간이 타임아웃(분) 이내면 만료되지 않는다")
    void 만료되지_않은_세션() {
        Session session = new Session();
        session.setTimeout(30);

        assertFalse(session.isExpired());
    }

    @Test
    @DisplayName("접근 시간이 타임아웃(분)을 넘으면 만료된다")
    void 만료된_세션() {
        Session session = new Session();
        session.setTimeout(30);
        session.setAccessTime(LocalDateTime.now().minusMinutes(31));

        assertTrue(session.isExpired());
    }

    @Test
    @DisplayName("타임아웃이 큰 값이어도 오버플로 없이 계산된다")
    void 큰_타임아웃_오버플로_없음() {
        Session session = new Session();
        session.setTimeout(100_000);

        assertFalse(session.isExpired());
    }

    @Test
    @DisplayName("존재하지 않는 세션 아이디를 조회하면 NPE 없이 null을 반환한다")
    void 없는_세션_조회() {
        assertNull(SessionManager.getSession("no-such-session-id"));
    }

    @Test
    @DisplayName("만료된 세션은 조회 시 제거되고 null을 반환한다")
    void 만료된_세션_조회() {
        Session session = new Session();
        session.setAccessTime(LocalDateTime.now().minusMinutes(31));

        assertNull(SessionManager.getSession(session.getId()));
    }

}
