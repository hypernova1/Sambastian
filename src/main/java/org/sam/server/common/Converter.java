package org.sam.server.common;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

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

    public static <T> T jsonToObject(String json, Class<T> type) {
        Gson gson = new Gson();
        return gson.fromJson(json, type);
    }

}
