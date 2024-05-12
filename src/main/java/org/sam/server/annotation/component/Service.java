package org.sam.server.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클래스에 선언 하여 해당 클래스를 빈으로 만든다. Component 어노테이션과 기능상 차이는 없다.
 *
 * @author hypernova1
 * @see org.sam.server.annotation.component.Component
 * */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Service {
}
