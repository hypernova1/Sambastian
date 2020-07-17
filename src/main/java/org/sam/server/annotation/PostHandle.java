package org.sam.server.annotation;

import org.sam.server.constant.ContentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:44 PM
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PostHandle {
    String value() default "/";
    ContentType contentType() default ContentType.JSON;
}
