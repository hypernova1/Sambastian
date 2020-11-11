package org.sam.server.annotation.handle;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 핸들러 클래스의 메소드에 선언하여 HTTP Method 중 PUT 요청을 처리합니다.
 *
 * @author hypernova1
 * @see org.sam.server.annotation.handle.Handle
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Handle
public @interface PutHandle {

    /*
     * 받을 수 있는 URL입니다.
     * */
    String value() default "/";

    /**
     * 받을 수 있는 미디어 타입입니다.
     * */
    ContentType contentType() default ContentType.APPLICATION_JSON;

    /**
     * 받을 수 있는 HTTP Method 입니다.
     * */
    HttpMethod method() default HttpMethod.PUT;
}
