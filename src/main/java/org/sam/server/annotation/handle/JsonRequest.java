package org.sam.server.annotation.handle;

import org.sam.server.constant.ContentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클라이언트에서 JSON으로 요청을 보내면 핸들러의 파라미터에 달아서 JSON을 해석하여 해당 타입으로 변환하도록 한다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JsonRequest {

    /*
    * 받을 수 있는 미디어 타입
    * */
    ContentType contentType() default ContentType.APPLICATION_JSON;
}
