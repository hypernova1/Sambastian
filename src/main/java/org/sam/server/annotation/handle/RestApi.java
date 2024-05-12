package org.sam.server.annotation.handle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * 핸들러 메서드 위에 선언을 하여 API 요청을 처리할 수 있도록 한다.
 *
 * @author hypernova1
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface RestApi {
}
