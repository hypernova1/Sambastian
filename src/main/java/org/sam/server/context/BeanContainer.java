package org.sam.server.context;

import org.sam.server.http.Interceptor;

import java.util.*;

/**
 * 빈을 관리하는 클래스 BeanContainer는 이미 선언 된 클래스를 기반으로 빈을 생성하지만
 * BeanFactory는 런타임시 동적으로 빈을 관리한다.
 *
 * @author hypernova1
 * @see BeanLoader
 * */
public class BeanContainer {

    private final Map<Class<?>, List<BeanDefinition>> beanDefinitionMap;

    private final List<Object> handlerBeans;

    private final List<Interceptor> interceptors;

    public BeanContainer(Map<Class<?>, List<BeanDefinition>> beanDefinitionMap, List<Object> handlerBeans, List<Interceptor> interceptors) {
        this.beanDefinitionMap = beanDefinitionMap;
        this.handlerBeans = handlerBeans;
        this.interceptors = interceptors;
//        this.beanLoader.getBeanDefinitionMap().put(BeanContainer.class, list);
    }

    /**
     * 인자로 받은 이름과, 타입에 해당하는 빈을 반환한다.
     *
     * @param <T> 빈 클래스 타입
     * @param name 빈 이름
     * @param type 빈 클래스
     * @return 빈
     * */
    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<?> type) {
        List<BeanDefinition> beanDefinitions = this.beanDefinitionMap.get(type);
        BeanDefinition savedBeanDefinition = beanDefinitions.stream()
                .filter(beanDefinition -> beanDefinition.getBeanName().equals(name))
                .findFirst()
                .orElse(null);
        return (T) savedBeanDefinition;
    }

    /**
     * 인자로 받은 타입에 해당하는 빈 목록을 반환한다.
     *
     * @param type 빈 타입
     * @return 빈 목록
     * */
    public List<?> getBeanList(Class<?> type) {
        Set<Class<?>> classes = this.beanDefinitionMap.keySet();
        Class<?> beanType = classes.stream()
                .filter(savedBeanType -> savedBeanType.isAssignableFrom(type))
                .findFirst()
                .orElseGet(() -> this.getMatchType(classes, type));
        List<BeanDefinition> beanDefinitions = this.beanDefinitionMap.get(beanType);
        List<Object> result = new ArrayList<>();
        for (BeanDefinition beanDefinition : beanDefinitions) {
            result.add(beanDefinition.getBeanInstance());
        }
        return result;
    }

    /**
     * 인자로 받은 인스턴스를 빈으로 만들어 저장한다.
     *
     * @param <T> 인스턴스 타입
     * @param name 빈 이름
     * @param instance 인스턴스
     * */
    public <T> void registerBean(String name, T instance) {
        List<BeanDefinition> list = Optional
                .ofNullable(getBeanDefinitionList(instance.getClass()))
                .orElseGet(ArrayList::new);
        boolean exists = list.stream()
                .anyMatch(beanDefinition -> beanDefinition.getBeanName().equals(name));
        if (exists) return;
        BeanDefinition beanDefinition = BeanDefinition.of(name, instance);
        list.add(beanDefinition);
    }

    /**
     * 해당 클래스 타입과 일치하는 빈의 클래스 타입을 반환한다.
     *
     * @param classes 빈 클래스 타입 목록
     * @param type 확인할 클래스 타입
     * @return 일치하는 클래스 타입
     * */
    private Class<?> getMatchType(Set<Class<?>> classes, Class<?> type) {
        for (Class<?> savedClass : classes) {
            Class<?>[] interfaces = savedClass.getInterfaces();
            for (Class<?> interfaceClass : interfaces) {
                if (!interfaceClass.equals(type)) continue;
                return savedClass;
            }
        }
        return null;
    }

    public List<BeanDefinition> getBeanDefinitionList(Class<?> type) {
        return this.beanDefinitionMap.get(type);
    }

    public Object findHandlerByClass(Class<?> clazz) {
        return this.handlerBeans.stream().filter((b) -> b.getClass().equals(clazz)).findFirst().orElse(null);
    }

    /**
     * 핸들러 빈 목록을 반환한다.
     *
     * @return 핸들러 빈 목록
     */
    public List<Object> getHandlerBeans() {
        return this.handlerBeans;
    }

    /**
     * 인터셉터 구현체 인스턴스 목록을 반환한다.
     *
     * @return 인터셉터 구현체 인스턴스
     */
    public List<Interceptor> getInterceptors() {
        return this.interceptors;
    }

    /**
     * 빈 목록을 반환한다.
     *
     * @return 빈 목록
     */
    public Map<Class<?>, List<BeanDefinition>> getBeanDefinitionMap() {
        return this.beanDefinitionMap;
    }

}
