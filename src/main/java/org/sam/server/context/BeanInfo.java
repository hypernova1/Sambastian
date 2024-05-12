package org.sam.server.context;

/**
 * 빈의 정보를 저장하는 클래스
 *
 * @author hypernova1
 */
public class BeanInfo {

    private final String name;
    private final Object instance;

    private BeanInfo(String name, Object instance) {
        this.name = name;
        this.instance = instance;
    }

    protected static BeanInfo of(String name, Object instance) {
        return new BeanInfo(name, instance);
    }

    /**
     * 빈의 이름을 반환한다.
     *
     * @return 빈 이름
     * */
    public String getBeanName() {
        return name;
    }

    /**
     * 빈을 반환한다.
     *
     * @return 빈 인스턴스
     * */
    public Object getBeanInstance() {
        return instance;
    }
}
