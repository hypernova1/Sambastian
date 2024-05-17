package org.sam.server.context;

/**
 * 컴포넌트 안에 있는 메서드 빈 생성시 접근 제어자가 public이 아닐 경우 발생한다.
 *
 * @author hypernova1
 * */
public class BeanAccessModifierException extends RuntimeException {
    public BeanAccessModifierException() {
        super("access modifier of bean's method must be public.");
    }
}
