package org.sam.server.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by melchor
 * Date: 2020/08/12
 * Time: 5:01 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebFilter {
    String value() default "/*";
}
