package org.sam.server.core;

import org.apache.log4j.Logger;
import org.sam.server.exception.BeanNotFoundException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;

public class BeanContainer {

    private static final Logger logger = Logger.getLogger(BeanContainer.class);
    private static final Map<Class<?>, List<Bean>> beanMap = new HashMap<>();
    private static final List<Object> handlerBeans = new ArrayList<>();

    private static List<Class<?>> componentClasses = BeanClassLoader.getComponentClasses();
    private static List<Class<?>> handlerClasses = BeanClassLoader.getHandlerClasses();

    public static void createBeans() {
        createComponentBeans();
        createHandlerBeans();
    }

    private static void createComponentBeans() {
        componentClasses.forEach(componentClass  -> {
            try {
                Object instance = createBean(componentClass);
                String componentName = componentClass.getName();
                Bean bean = new Bean(componentName, instance);
                logger.info("create bean: " + bean.getName());
                if (beanMap.get(componentClass) != null)
                    beanMap.get(componentClass).add(bean);
                else {
                    List<Bean> beanList = new ArrayList<>();
                    beanList.add(bean);
                    beanMap.put(componentClass, beanList);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createHandlerBeans() {
        handlerClasses.forEach(handlerClass -> {
            try {
                Object bean = createBean(handlerClass);
                logger.info("create handler bean: " + handlerClass.getName());
                handlerBeans.add(bean);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static Object createBean(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor;
        if (constructors.length == 0)
            constructor = clazz.getDeclaredConstructor();
        else
            constructor = constructors[0];
        Parameter[] parameters = constructor.getParameters();
        List<Object> parameterList = createParameters(parameters);

        return constructor.newInstance(parameterList.toArray());
    }

    private static List<Object> createParameters(Parameter[] parameters) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Object> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            try {
                Object bean = findBean(parameter.getType(), parameterName);
                if (bean == null)
                    bean = createBean(parameter.getType());
                parameterList.add(bean);
            } catch (BeanNotFoundException e) {
                e.printStackTrace();
            }
        }
        return parameterList;
    }

    private static Object findBean(Class<?> type, String parameterName) throws BeanNotFoundException {
        if (!componentClasses.contains(type))
            type = findSuperClass(type);
        List<Bean> beans = beanMap.get(type);
        if (beans == null) return null;
        if (beans.size() == 1)
            return beans.get(0);
        for (Bean bean : beans) {
            if (bean.getName().equals(parameterName))
                return bean;
        }
        return null;
    }

    private static Class<?> findSuperClass(Class<?> type) throws BeanNotFoundException {
        Set<Class<?>> keys = beanMap.keySet();
        for (Class<?> key : keys) {
            if (key.isAssignableFrom(type)) {
                return key;
            }
        }
        throw new BeanNotFoundException(type.getName() + " bean is not found");
    }

    public static Map<Class<?>, List<Bean>> getBeanMap() {
        return beanMap;
    }

    public static List<Object> getHandlerBeans() {
        return handlerBeans;
    }
}
