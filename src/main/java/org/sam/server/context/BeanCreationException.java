package org.sam.server.context;

/**
 * 해당 빈을 생성할 수 없을 때 발생한다.
 *
 * @author hypernova1
 * */
public class BeanCreationException extends RuntimeException {

    public BeanCreationException(Class<?> clazz) {
        super("Bean cannot be created: " + clazz.getName());
    }

}
