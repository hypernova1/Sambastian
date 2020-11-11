package org.sam.server.annotation.handle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 핸들러의 파라미터에 선언하여 쿼리스트링의 값을 매핑할 수 있게 합니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequiredParam {

    /**
     * 파라미터 이름
     * */
    boolean value() default true;
}
