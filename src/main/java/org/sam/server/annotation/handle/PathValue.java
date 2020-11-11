package org.sam.server.annotation.handle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * URL안에 파라미터가 존재할 때, 핸들러의 파라미터에 선언하면 이름이 같은 변수에 값을 할당해준다.
 *
 * @author hypernova1
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface PathValue {
}
