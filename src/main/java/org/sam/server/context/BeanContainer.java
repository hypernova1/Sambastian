package org.sam.server.context;

import org.sam.server.annotation.Qualifier;
import org.sam.server.annotation.component.Bean;
import org.sam.server.exception.BeanAccessModifierException;
import org.sam.server.exception.BeanNotFoundException;
import org.sam.server.http.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class BeanContainer {

    private static final Logger logger = LoggerFactory.getLogger(BeanContainer.class);
    private static final Map<Class<?>, List<BeanInfo>> beanMap = new HashMap<>();
    private static final List<Object> handlerBeans = new ArrayList<>();
    private static final List<Interceptor> interceptors = new ArrayList<>();

    private static final List<Class<?>> componentClasses = BeanClassLoader.getComponentClasses();

    public static void createBeans() {
        createComponentBeans();
        createHandlerBeans();
        createInterceptor();
    }

    private static void createComponentBeans() {
        componentClasses.forEach(componentClass  -> {
            try {
                String beanName = getBeanName(componentClass);
                if (!isDuplicated(beanName, componentClass)) {
                    Object beanInstance = createBeanInstance(componentClass);
                    Method[] declaredMethods = beanInstance.getClass().getDeclaredMethods();
                    createMethodBean(beanInstance, declaredMethods);
                    addBeanMap(componentClass, beanInstance, beanName);
                }
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createMethodBean(Object beanInstance, Method[] declaredMethods) {
        Arrays.stream(declaredMethods).forEach(declaredMethod -> {
            if (declaredMethod.getDeclaredAnnotation(Bean.class) != null) {
                try {
                    Class<?> beanType = declaredMethod.getReturnType();
                    Object instance = declaredMethod.invoke(beanInstance);
                    String beanName = declaredMethod.getName();
                    addBeanMap(beanType, instance, beanName);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new BeanAccessModifierException();
                }
            }
        });
    }

    private static void addBeanMap(Class<?> componentClass, Object beanInstance, String beanName) {
        BeanInfo beanInfo = new BeanInfo(beanName, beanInstance);
        logger.info("create bean: " + beanName + " > " + componentClass.getName());
        if (beanMap.get(componentClass) != null)
            beanMap.get(componentClass).add(beanInfo);
        else {
            List<BeanInfo> beanInfoList = new ArrayList<>();
            beanInfoList.add(beanInfo);
            beanMap.put(componentClass, beanInfoList);
        }
    }

    private static void createHandlerBeans() {
        BeanClassLoader.getHandlerClasses().forEach(handlerClass -> {
            try {
                Object bean = createBeanInstance(handlerClass);
                logger.info("create handler bean: " + handlerClass.getName());
                handlerBeans.add(bean);
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        });
    }

    private static void createInterceptor() {
        BeanClassLoader.getInterceptorClasses().forEach(interceptorClass -> {
            try {
                Interceptor interceptor = (Interceptor) interceptorClass.getDeclaredConstructor().newInstance();
                interceptors.add(interceptor);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        });
    }

    private static Object createBeanInstance(Class<?> clazz) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Constructor<?>[] constructors = clazz.getConstructors();
        Constructor<?> constructor;
        if (constructors.length == 0)
            constructor = clazz.getDeclaredConstructor();
        else
            constructor = constructors[0];
        Parameter[] parameters = constructor.getParameters();
        List<Object> parameterList = createParameters(parameters);
        if (parameters.length > parameterList.size()) {
            int differenceCount = parameters.length - parameterList.size();
            for (int i = 0; i < differenceCount; i++) {
                parameterList.add(null);
            }
        }
        return constructor.newInstance(parameterList.toArray());
    }

    private static List<Object> createParameters(Parameter[] parameters) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        List<Object> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String parameterName = parameter.getName();
            try {
                BeanInfo beanInfo = findBean(parameter.getType(), parameterName);
                if (beanInfo == null) {
                    int index = componentClasses.indexOf(parameter.getType());
                    if (index == -1) continue;
                    Class<?> beanClass = componentClasses.get(index);
                    String beanName;
                    beanName = getBeanName(beanClass);
                    Object beanInstance = createBeanInstance(beanClass);
                    beanInfo = new BeanInfo(beanName, beanInstance);
                    addBeanMap(parameter.getType(), beanInstance, parameterName);
                }
                parameterList.add(beanInfo.getInstance());
            } catch (BeanNotFoundException e) {
                e.printStackTrace();
            }
        }
        return parameterList;
    }

    private static String getBeanName(Class<?> beanClass) {
        Qualifier qualifier = beanClass.getDeclaredAnnotation(Qualifier.class);
        if (qualifier != null) {
            return qualifier.value();
        }
        String beanName = beanClass.getSimpleName();
        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
    }

    private static boolean isDuplicated(String beanName, Class<?> clazz) {
        List<BeanInfo> beanInfos = beanMap.get(clazz);
        if (beanInfos != null) {
            for (BeanInfo beanInfo : beanInfos) {
                if (beanInfo.getName().equals(beanName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static BeanInfo findBean(Class<?> type, String parameterName) throws BeanNotFoundException {
        if (!componentClasses.contains(type))
            type = findSuperClass(type);
        List<BeanInfo> beanInfos = beanMap.get(type);
        if (beanInfos == null) return null;
        if (beanInfos.size() == 1)
            return beanInfos.get(0);
        for (BeanInfo beanInfo : beanInfos) {
            if (beanInfo.getName().equals(parameterName))
                return beanInfo;
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
//        throw new BeanNotFoundException(type.getName());
        return null;
    }

    public static List<Object> getHandlerBeans() {
        return handlerBeans;
    }

    public static List<Interceptor> getInterceptors() {
        return interceptors;
    }

    static Map<Class<?>, List<BeanInfo>> getBeanMap() {
        return beanMap;
    }
}
