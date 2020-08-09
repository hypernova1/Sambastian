package org.sam.server.core;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanContainer {

    private final Map<String, Object> beanMap = new HashMap<>();

    public void inject() {

        Map<String, Class<?>> componentBeans = BeanClassLoader.getComponentBeans();
        List<Class<?>> handlerClasses = BeanClassLoader.getHandlerClasses();

        createBean(componentBeans);
    }

    private void createBean(Map<String, Class<?>> componentBeans) {
        componentBeans.forEach((key, value)  -> {


        });
    }

}
