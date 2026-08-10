package org.sam.server.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayUtilsTest {

    @Test
    @DisplayName("getDoubleArray는 두 배 길이의 배열에 기존 데이터를 복사해 반환한다")
    void getDoubleArray_두배_확장() {
        byte[] origin = {1, 2, 3};

        byte[] result = ArrayUtils.getDoubleArray(origin);

        assertEquals(6, result.length);
        assertArrayEquals(new byte[]{1, 2, 3, 0, 0, 0}, result);
    }

    @Test
    @DisplayName("isFullCapacity는 배열 길이와 사용 길이가 같을 때만 true를 반환한다")
    void isFullCapacity_판정() {
        byte[] data = new byte[4];

        assertTrue(ArrayUtils.isFullCapacity(data, 4));
        assertFalse(ArrayUtils.isFullCapacity(data, 3));
    }

}
