package org.sam.server.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/08/29
 * Time: 7:41 PM
 */
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

    public static BeanFactory getInstance() {
        return beanFactory;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(String name, Class<?> clazz) {
        List<BeanInfo> beanInfos = BeanContainer.getBeanMap().get(clazz);
        BeanInfo savedBeanInfo = beanInfos.stream()
                .filter(beanInfo -> beanInfo.getName().equals(name))
                .findFirst()
                .orElse(null);
        return (T) savedBeanInfo;
    }

    public List<?> getBeanList(Class<?> clazz) {
        Set<Class<?>> classes = BeanContainer.getBeanMap().keySet();
        Class<?> beanType = classes.stream()
                .filter(savedBeanType -> savedBeanType.isAssignableFrom(clazz))
                .findFirst()
                .orElseGet(() -> {
                    for (Class<?> savedClass : classes) {
                        Class<?>[] interfaces = savedClass.getInterfaces();
                        for (Class<?> interfaze : interfaces) {
                            if (interfaze.equals(clazz)) {
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
