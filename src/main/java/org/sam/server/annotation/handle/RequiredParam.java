package org.sam.server.annotation.handle;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 9:36 PM
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface RequiredParam {
    boolean value() default true;
}
