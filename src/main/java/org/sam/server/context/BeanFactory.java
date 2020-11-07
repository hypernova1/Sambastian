package org.sam.server.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/08/29
 * Time: 7:41 PM
 */
public class BeanFactory {

    private static BeanFactory beanFactory;

    static {
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
        Bean savedBean = beans.stream()
                .filter(bean -> bean.getName().equals(name)).findFirst().orElseGet(() -> null);
        if (savedBean == null) return null;
        return (T) savedBean;
    }

    public List<?> getBeanList(Class<?> clazz) {
        Set<Class<?>> classes = BeanContainer.getBeanMap().keySet();
        Class<?> beanType = classes.stream()
                .filter(savedBeanType -> savedBeanType.isAssignableFrom(clazz)).findFirst().orElse(null);
        if (beanType == null) {
            for (Class<?> savedClass : classes) {
                Class<?>[] interfaces = savedClass.getInterfaces();
                for (Class<?> interfaze : interfaces) {
                    if (interfaze.equals(clazz)) {
                        beanType = savedClass;
                        break;
                    }
                }
            }
        }
        List<Bean> beans = BeanContainer.getBeanMap().get(beanType);
        List<Object> result = new ArrayList<>();
        beans.forEach(bean -> result.add(bean.getInstance()));

        return result;
    }

    public <T> void registerBean(String name, T instance) {
        List<Bean> list = BeanContainer.getBeanMap().get(instance.getClass());
        if (list == null)
            list = new ArrayList<>();
        boolean isExist = list.stream().anyMatch(bean -> bean.getName().equals(name));
        if (isExist) return;
        Bean bean = new Bean(name, instance);
        list.add(bean);
    }

}
