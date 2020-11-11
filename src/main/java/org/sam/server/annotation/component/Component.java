package org.sam.server.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클래스에 선언 하여 해당 클래스를 빈으로 만듭니다.
 *
 * @author hypernova1
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
}
