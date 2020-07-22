package org.sam.server.annotation.handle;

import org.sam.server.constant.ContentType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 4:33 PM
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface JsonRequest {
    ContentType contentType() default ContentType.APPLICATION_JSON;
}
