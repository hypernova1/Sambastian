package org.sam.server.util;

import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 요청 파라미터나 JSON을 인스턴슫로 변환하는 클래스입니다.
 *
 * @author hypernova1
 */
public class Converter {

    private static final Gson gson = new Gson();

    /**
     * 파라미터를 받아 인스턴스에 값을 넣어주고 인스턴스를 반환합니다.
     *
     * @param parameters 요청 파라미터
     * @param type 핸들러의 파라미터 타입
     * @return 파라미턴 인스턴스
     * */
    public static Object parameterToObject(Map<String, String> parameters, Class<?> type) {
        Object instance = null;
        try {
            instance = type.getDeclaredConstructor().newInstance();
            List<Method> setters = Arrays.stream(type.getMethods())
                    .filter(m -> m.getName().startsWith("set"))
                    .collect(Collectors.toList());
            for (Method setter : setters) {
                invokeSetterMethod(parameters, instance, setter);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * JSON을 인스턴스로 바꾸고 반환합니다.
     *
     * @param json 요청 JSON
     * @param type 핸들러 파라미터 타입
     * @return 파라미터 인스턴스
     * */
    public static <T> T jsonToObject(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }

    /**
     * Setter 메서드를 실행 시킵니다.
     *
     * @param parameters Setter의 파라미터
     * @param instance 실행할 인스턴스
     * @param method Setter 메서드
     * */
    private static void invokeSetterMethod(Map<String, String> parameters, Object instance, Method method) throws IllegalAccessException, InvocationTargetException {
        String propertyName = method.getName().replace("set", "");
        propertyName = propertyName.substring(0, 1).toLowerCase() + propertyName.substring(1);
        String parameter = parameters.get(propertyName);
        method.invoke(instance, parameter);
    }

    /**
     * 인스턴를 JSON으로 변환합니다.
     *
     * @param object 인스턴스
     * @return JSON
     * */
    public static String objectToJson(Object object) {
        return gson.toJson(object);
    }

}
