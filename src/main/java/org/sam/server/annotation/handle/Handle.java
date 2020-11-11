package org.sam.server.annotation.handle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 각 HTTP Method에 관련된 어노테이션의 부모 어노테이션입니다.
 * */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Handle {
}
