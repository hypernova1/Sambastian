package org.sam.server.core;

import org.apache.log4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeanContainer {

    private static final Logger logger = Logger.getLogger(BeanContainer.class);
    private static final Map<Class<?>, List<Bean>> beanMap = new HashMap<>();

    public static void inject() {
        List<Class<?>> componentClasses = BeanClassLoader.getComponentClasses();
        System.out.println(componentClasses.size());
        List<Class<?>> handlerClasses = BeanClassLoader.getHandlerClasses();
        createBeans(componentClasses);
    }

    private static void createBeans(List<Class<?>> componentClasses) {
        componentClasses.forEach(componentClass  -> {
            try {
                Object instance = createBean(componentClass);
                String componentName = componentClass.getName();
                Bean bean = new Bean(componentName, instance);
                logger.info("create bean " + bean.getName());
                if (beanMap.get(componentClass) != null) {
                    beanMap.get(componentClass).add(bean);
                } else {
                    List<Bean> beanList = new ArrayList<>();
                    beanList.add(bean);
                    beanMap.put(componentClass, beanList);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static Object createBean(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor;
        if (constructors.length == 0) {
            constructor = clazz.getDeclaredConstructor();
        } else {
            constructor = constructors[0];
        }
        Parameter[] parameters = constructor.getParameters();
        List<Object> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            Object bean = findBean(parameter.getType(), parameterName);
            if (bean == null) {
                bean = createBean(parameter.getType());
            }
            parameterList.add(bean);
        }

        return constructor.newInstance(parameterList.toArray());
    }

    private static Object findBean(Class<?> type, String parameterName) {
        List<Bean> beans = beanMap.get(type);
        if (beans == null) return null;
        if (beans.size() == 1) return beans.get(0);
        for (Bean bean : beans) {
            if (bean.getName().equals(parameterName)) {
                return bean;
            }
        }
        return null;
    }

}
