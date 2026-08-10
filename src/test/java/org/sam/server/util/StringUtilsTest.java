package org.sam.server.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    @DisplayName("null이나 빈 문자열이면 isEmpty가 true를 반환한다")
    void isEmpty_null_또는_빈문자열() {
        assertTrue(StringUtils.isEmpty(null));
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    @DisplayName("값이 있는 문자열이면 isEmpty가 false를 반환한다")
    void isEmpty_값이_있는_문자열() {
        assertFalse(StringUtils.isEmpty("sambastian"));
        assertFalse(StringUtils.isEmpty(" "));
    }

    @Test
    @DisplayName("isNotEmpty는 isEmpty의 반대 결과를 반환한다")
    void isNotEmpty_동작() {
        assertTrue(StringUtils.isNotEmpty("a"));
        assertFalse(StringUtils.isNotEmpty(null));
        assertFalse(StringUtils.isNotEmpty(""));
    }

}
