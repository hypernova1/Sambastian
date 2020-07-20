package org.sam.server.common;

import org.sam.server.http.Request;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 3:54 PM
 */
public class Converter {

    public static <T> T parameterToObject(Map<String, String> parameters, Class<T> type) {
        T instance = null;
        try {
            instance = type.newInstance();
            for (Method declaredMethod : type.getDeclaredMethods()) {
                String methodName = declaredMethod.getName();
                if (methodName.startsWith("set")) {
                    String propertyName = methodName.replace("set", "");
                    propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
                    String parameter = parameters.get(propertyName);
                        declaredMethod.invoke(instance, parameter);
                }
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return instance;
    }

    public static <T> void jsonToObject(String json, Class<T> type) {

    }

}
