package org.sam.server.annotation.handle;

import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 핸들러 클래스의 메소드에 선언하여 HTTP Method 중 GET 요청을 처리한다.
 *
 * @author hypernova1
 * @see RequestMapping
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@RequestMapping
public @interface GetMapping {

    /*
     * 받을 수 있는 URL.
     * */
    String value() default "/";

    /**
     * 받을 수 있는 미디어 타입
     *
     * @return 받을 수 있는 미디어 타입
     * */
    ContentType contentType() default ContentType.APPLICATION_JSON;

    /**
     * 받을 수 있는 HTTP Method
     *
     * @return 받을 수 있는 HTTP Method
     * */
    HttpMethod method() default HttpMethod.GET;
}
