package org.sam.server.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 20:24 PM
 */
public class PrimitiveWrapper {

    private static final Map<String, Class<?>> map = new HashMap<String, Class<?>>(9);

    static {
        map.put(Boolean.TYPE.getName(), Boolean.class);
        map.put(Character.TYPE.getName(), Character.class);
        map.put(Byte.TYPE.getName(), Byte.class);
        map.put(Short.TYPE.getName(), Short.class);
        map.put(Integer.TYPE.getName(), Integer.class);
        map.put(Long.TYPE.getName(), Long.class);
        map.put(Float.TYPE.getName(), Float.class);
        map.put(Double.TYPE.getName(), Double.class);
        map.put(Void.TYPE.getName(), Void.class);
    }

    private static Object autoBoxing(Class<?> type, String value) {
        Constructor<?>[] constructors = type.getConstructors();
        for (Constructor<?> constructor : constructors) {
            Parameter[] parameters = constructor.getParameters();
            for (Parameter parameter : parameters) {
                if (parameter.getType().equals(String.class)) {
                    try {
                        return constructor.newInstance(value);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    public static Class<?> getType(String type) {
        return map.get(type);
    }

    public static Object wrapPrimitiveValue(Class<?> parameter, String value) {
        String parameterType = parameter.getSimpleName();
        Class<?> type = PrimitiveWrapper.getType(parameterType);
        return PrimitiveWrapper.autoBoxing(type, value);
    }
}