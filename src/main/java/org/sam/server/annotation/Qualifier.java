package org.sam.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 컴포넌트에 선언하여 빈의 이름을 정의거나 생성자 파라미터에 선언하여 받을 빈의 이름을 명시합니다.
 *
 * @author hypernova1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface Qualifier {
    String value() default "";
}
