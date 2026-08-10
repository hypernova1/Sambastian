package org.sam.server.http;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieStoreTest {

    @Test
    @DisplayName("Cookie 헤더 문자열을 쿠키 목록으로 파싱한다")
    void 쿠키_파싱() {
        Set<Cookie> cookies = CookieStore.parseCookie("sessionId=abc123; theme=dark");

        Map<String, String> byName = toMap(cookies);
        assertEquals(2, cookies.size());
        assertEquals("abc123", byName.get("sessionId"));
        assertEquals("dark", byName.get("theme"));
    }

    @Test
    @DisplayName("값에 =가 포함된 쿠키는 첫 번째 =만 구분자로 사용한다")
    void 값에_등호_포함() {
        Set<Cookie> cookies = CookieStore.parseCookie("token=a=b=c");

        assertEquals("a=b=c", toMap(cookies).get("token"));
    }

    @Test
    @DisplayName("값이 없거나 형식이 잘못된 쿠키는 예외 없이 건너뛴다")
    void 잘못된_형식_스킵() {
        Set<Cookie> cookies = CookieStore.parseCookie("flag; =novalue; valid=1");

        assertEquals(1, cookies.size());
        assertEquals("1", toMap(cookies).get("valid"));
    }

    @Test
    @DisplayName("공백 없이 ;로만 구분된 쿠키도 파싱한다")
    void 공백없는_구분자() {
        Set<Cookie> cookies = CookieStore.parseCookie("a=1;b=2");

        assertEquals(2, cookies.size());
    }

    @Test
    @DisplayName("호출할 때마다 독립적인 새 컬렉션을 반환한다 (전역 상태 없음)")
    void 전역_상태_없음() {
        Set<Cookie> first = CookieStore.parseCookie("a=1");
        Set<Cookie> second = CookieStore.parseCookie("b=2");

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertTrue(toMap(second).containsKey("b"));
        assertTrue(!toMap(second).containsKey("a"));
    }

    private Map<String, String> toMap(Set<Cookie> cookies) {
        return cookies.stream().collect(Collectors.toMap(Cookie::getName, Cookie::getValue, (a, b) -> a));
    }

}
