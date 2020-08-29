package org.sam.server.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/08/29
 * Time: 7:41 PM
 */
public class BeanFactory {

    private static BeanFactory beanFactory;

    {
        beanFactory = new BeanFactory();
        List<Bean> list = new ArrayList<>();
        Bean bean = new Bean("beanFactory", beanFactory);
        list.add(bean);
        BeanContainer.getBeanMap().put(BeanFactory.class, list);
    }

    private BeanFactory() {}

    public static BeanFactory getInstance() {
        return beanFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<?> clazz) {
        List<Bean> beans = BeanContainer.getBeanMap().get(clazz);
        Bean savedBean = beans.stream().filter(bean -> bean.getName().equals(name)).findFirst().orElseGet(() -> null);
        if (savedBean == null) return null;
        return (T) savedBean;
    }

    public <T> void insertBean(String name, Class<?> clazz) {

    }

}
