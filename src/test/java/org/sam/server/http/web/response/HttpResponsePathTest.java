package org.sam.server.http.web.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpResponsePathTest {

    @Test
    @DisplayName("상위 디렉터리 탈출 경로는 차단한다")
    void 디렉터리_탈출_차단() {
        assertTrue(HttpResponse.isForbiddenPath("/resources/../../../etc/passwd"));
        assertTrue(HttpResponse.isForbiddenPath("../secret.txt"));
        assertTrue(HttpResponse.isForbiddenPath("static/..\\..\\windows\\system32"));
    }

    @Test
    @DisplayName("퍼센트 인코딩으로 우회한 탈출 경로도 차단한다")
    void 인코딩_우회_차단() {
        assertTrue(HttpResponse.isForbiddenPath("/resources/%2e%2e/%2e%2e/etc/passwd"));
        assertTrue(HttpResponse.isForbiddenPath("/resources/%252e%252e/secret"));
    }

    @Test
    @DisplayName("널 바이트가 포함된 경로는 차단한다")
    void 널바이트_차단() {
        assertTrue(HttpResponse.isForbiddenPath("/resources/static/a%00.html"));
    }

    @Test
    @DisplayName("정상적인 정적 리소스 경로는 허용한다")
    void 정상_경로_허용() {
        assertFalse(HttpResponse.isForbiddenPath("/resources/static/app.css"));
        assertFalse(HttpResponse.isForbiddenPath("static/index.html"));
        assertFalse(HttpResponse.isForbiddenPath("favicon.ico"));
    }

}
