package org.sam.server.annotation.component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Handler {
    String value() default "/";
}
