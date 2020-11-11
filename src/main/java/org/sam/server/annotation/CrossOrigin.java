package org.sam.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 허용 가능한 IP 목록을 정의합니다. 와일드카드(*) 선언시 모든 IP를 허용합니다.
 *
 * @author hypernova1
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CrossOrigin {

    /**
     * 허용할 IP 목록
     * */
    String[] value() default "";
}
