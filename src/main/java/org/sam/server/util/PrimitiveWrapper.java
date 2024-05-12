package org.sam.server.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * 원시 타입을 박스 타입으로 변환해주는 클래스
 */
public class PrimitiveWrapper {

    private static final Map<String, Class<?>> boxTypes = new HashMap<>(9);

    static {
        boxTypes.put(Boolean.TYPE.getName(), Boolean.class);
        boxTypes.put(Character.TYPE.getName(), Character.class);
        boxTypes.put(Byte.TYPE.getName(), Byte.class);
        boxTypes.put(Short.TYPE.getName(), Short.class);
        boxTypes.put(Integer.TYPE.getName(), Integer.class);
        boxTypes.put(Long.TYPE.getName(), Long.class);
        boxTypes.put(Float.TYPE.getName(), Float.class);
        boxTypes.put(Double.TYPE.getName(), Double.class);
        boxTypes.put(Void.TYPE.getName(), Void.class);
    }

    /**
     * 원시 타입의 값을 박스 타입 인스턴스로 변환한다.
     *
     * @param type 원시 타입
     * @param value 원시 타입 값
     * @return 박스 타입 인스턴스
     * */
    public static Object wrapPrimitiveValue(Class<?> type, String value) {
        String parameterType = type.getSimpleName();
        Class<?> boxType = PrimitiveWrapper.getType(parameterType);
        return PrimitiveWrapper.autoBoxing(boxType, value);
    }

    /**
     * 원시 타입을 박스 타입으로 변환한다.
     *
     * @param type 박스 타입
     * @param value 원시 타입 값
     * @return 박스 타입 인스턴스
     * */
    private static Object autoBoxing(Class<?> type, String value) {
        Constructor<?>[] constructors = type.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                if (!parameter.getType().equals(String.class)) continue;
                try {
                    return constructor.newInstance(value);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return null;
    }

    /**
     * 원시 타입에 해당하는 박스 타입을 반환한다.
     *
     * @param type 원시타명
     * @return 박싱 인스턴스
     * */
    public static Class<?> getType(String type) {
        return boxTypes.get(type);
    }

}