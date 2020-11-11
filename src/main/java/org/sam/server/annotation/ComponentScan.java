package org.sam.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Root Package로 선언할 패키지의 클래스에 선언하여 동/하위 컴포넌트를 찾을 수 있게 해준다.
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ComponentScan {

}
