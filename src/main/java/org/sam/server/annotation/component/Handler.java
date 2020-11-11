package org.sam.server.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클래스 위에 선언하여 해당 클래스를 핸들러로 만듭니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {

    /**
     * 요청 URL의 앞 부분을 매핑합니다.
     * */
    String value() default "/";
}
