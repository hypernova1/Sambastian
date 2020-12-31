package org.sam.server.util;

import org.sam.server.context.BeanFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 인스턴스를 다른 인스턴스로 변환할 수 있게 하는 클래스입니다.
 *
 * @author hypernova1
 */
public class ModelMapper {

    /**
     * 인스턴스를 다른 클래스 타입으로 변환합니다.
     *
     * @param <T> 기존 인스턴스 타입
     * @param <U> 변경할 클래스 타입
     * @param instance 기존 인스턴스
     * @param clazz 변경할 클래스 타입
     * @return 변경된 인스턴스
     * */
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

    /**
     * 기존 인스턴스의 프로퍼티를 변경할 인스턴스에 설정합니다.
     *
     * @param <T> 기존 인스턴스 타입
     * @param <U> 변경할 인스턴스 타입
     * @param instance 기존 인스턴스
     * @param target 변경할 인스턴스
     * */
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
        List<?> beanList = beanFactory.getBeanList(CustomModelMapper.class);
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
