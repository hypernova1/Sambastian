package org.sam.server.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpStatusTest {

    @Test
    @DisplayName("4xx와 5xx 상태 코드는 에러로 판정한다")
    void isError_에러_코드() {
        assertTrue(HttpStatus.BAD_REQUEST.isError());
        assertTrue(HttpStatus.NOT_FOUND.isError());
        assertTrue(HttpStatus.INTERNAL_SERVER_ERROR.isError());
    }

    @Test
    @DisplayName("2xx 상태 코드는 에러가 아니다")
    void isError_정상_코드() {
        assertFalse(HttpStatus.OK.isError());
        assertFalse(HttpStatus.CREATED.isError());
        assertFalse(HttpStatus.NO_CONTENT.isError());
    }

    @Test
    @DisplayName("errorType은 코드 대역에 따라 NONE/CLIENT/SERVER를 반환한다")
    void errorType_분류() {
        assertEquals(HttpErrorType.NONE, HttpStatus.OK.errorType());
        assertEquals(HttpErrorType.CLIENT, HttpStatus.BAD_REQUEST.errorType());
        assertEquals(HttpErrorType.SERVER, HttpStatus.INTERNAL_SERVER_ERROR.errorType());
    }

}
