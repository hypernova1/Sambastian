package org.sam.server.context;

/**
 * 빈의 정보를 저장하는 클래스입니다.
 *
 * @author hypernova1
 */
public class BeanInfo {

    private final String name;

    private final Object instance;

    BeanInfo(String name, Object instance) {
        this.name = name;
        this.instance = instance;
    }

    /**
     * 빈의 이름을 반환합니다.
     *
     * @return 빈 이름
     * */
    public String getName() {
        return name;
    }

    /**
     * 빈을 반환합니다.
     *
     * @return 빈 인스턴스
     * */
    public Object getInstance() {
        return instance;
    }
}
