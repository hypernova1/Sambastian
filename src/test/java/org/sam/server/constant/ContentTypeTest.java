package org.sam.server.constant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentTypeTest {

    @Test
    @DisplayName("MIME 문자열로 해당하는 ContentType을 찾는다")
    void get_등록된_타입() {
        assertEquals(ContentType.APPLICATION_JSON, ContentType.get("application/json"));
        assertEquals(ContentType.TEXT_PLAIN, ContentType.get("text/plain"));
        assertEquals(ContentType.MULTIPART_FORM_DATA, ContentType.get("multipart/form-data"));
    }

    @Test
    @DisplayName("등록되지 않은 MIME 문자열은 TEXT_HTML로 폴백한다")
    void get_미등록_타입_폴백() {
        assertEquals(ContentType.TEXT_HTML, ContentType.get("image/webp"));
        assertEquals(ContentType.TEXT_HTML, ContentType.get(""));
    }

}
