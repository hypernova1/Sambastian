package org.sam.server.util;

import org.sam.server.context.BeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/08/28
 * Time: 10:43 PM
 */
public class ModelMapper {

    public <T, U> U convert(T instance, Class<U> clazz) {
        U target = null;
        try {
            target = clazz.getDeclaredConstructor().newInstance();
            setValue(instance, target);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return target;
    }

    private <T, U> void setValue(T instance, U target) {
        Method[] declaredMethods = instance.getClass().getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getName().startsWith("get")) {
                try {
                    String propertyName = declaredMethod.getName().replace("get", "");
                    Object value = declaredMethod.invoke(instance);
                    Method setter = target.getClass().getMethod("set" + propertyName, value.getClass());
                    setter.invoke(target, value);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                }
            }
        }

        BeanFactory beanFactory = BeanFactory.getInstance();
        List<Object> beanList = beanFactory.getBeanList(CustomModelMapper.class);
        if (beanList != null) {
            try {
                CustomModelMapper customModelMapper = (CustomModelMapper) beanList.get(0);
                Method map = customModelMapper.getClass().getMethod("map", instance.getClass(), target.getClass());
                map.invoke(customModelMapper, instance, target);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
    }

}
