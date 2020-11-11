package org.sam.server.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 빈을 관리하는 클래스입니다. BeanContainer는 이미 선언 된 클래스를 기반으로 빈을 생성하지만
 * BeanFactory는 런타임시 동적으로 빈을 관리합니다.
 *
 * @author hypernova1
 * @see org.sam.server.context.BeanContainer
 * */
public class BeanFactory {

    private static final BeanFactory beanFactory;

    static {
        beanFactory = new BeanFactory();
        List<BeanInfo> list = new ArrayList<>();
        BeanInfo beanInfo = new BeanInfo("beanFactory", beanFactory);
        list.add(beanInfo);
        BeanContainer.getBeanMap().put(BeanFactory.class, list);
    }

    private BeanFactory() {}

    /**
     * 인스턴스를 반환합니다.
     *
     * @return 인스턴스
     * */
    public static BeanFactory getInstance() {
        return beanFactory;
    }

    /**
     * 인자로 받은 이름과, 타입에 해당하는 빈을 반환합니다.
     *
     * @param name 빈 이름
     * @param type 빈 타입
     * @return 빈
     * */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<?> type) {
        List<BeanInfo> beanInfos = BeanContainer.getBeanMap().get(type);
        BeanInfo savedBeanInfo = beanInfos.stream()
                .filter(beanInfo -> beanInfo.getName().equals(name))
                .findFirst()
                .orElse(null);
        return (T) savedBeanInfo;
    }

    /**
     * 인자로 받은 타입에 해당하는 빈 목록을 반환합니다.
     *
     * @param type 빈 타입
     * @return 빈 목록
     * */
    public List<?> getBeanList(Class<?> type) {
        Set<Class<?>> classes = BeanContainer.getBeanMap().keySet();
        Class<?> beanType = classes.stream()
                .filter(savedBeanType -> savedBeanType.isAssignableFrom(type))
                .findFirst()
                .orElseGet(() -> {
                    for (Class<?> savedClass : classes) {
                        Class<?>[] interfaces = savedClass.getInterfaces();
                        for (Class<?> interfaceClass : interfaces) {
                            if (interfaceClass.equals(type)) {
                                return savedClass;
                            }
                        }
                    }
                    return null;
                });
        List<BeanInfo> beanInfos = BeanContainer.getBeanMap().get(beanType);
        List<Object> result = new ArrayList<>();
        beanInfos.forEach(beanInfo -> result.add(beanInfo.getInstance()));

        return result;
    }

    /**
     * 인자로 받은 인스턴스를 빈으로 만들어 저장합니다.
     *
     * @param name 빈 이름
     * @param instance 인스턴스
     * */
    public <T> void registerBean(String name, T instance) {
        List<BeanInfo> list = Optional
                .ofNullable(BeanContainer.getBeanMap().get(instance.getClass()))
                .orElseGet(ArrayList::new);
        boolean isExist = list.stream().anyMatch(beanInfo -> beanInfo.getName().equals(name));
        if (isExist) return;
        BeanInfo beanInfo = new BeanInfo(name, instance);
        list.add(beanInfo);
    }

}
