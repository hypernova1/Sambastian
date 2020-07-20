package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.annotation.handle.*;
import org.sam.server.common.Converter;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.Request;
import org.sam.server.http.Response;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/07/20
 * Time: 2:13 AM
 */
public class HandlerFinder {

    private final Request request;
    private final Response response;
    private final Gson gson;

    private List<Class<? extends Annotation>> handleAnnotations =
            Arrays.asList(GetHandle.class, PostHandle.class, PutHandle.class, DeleteHandle.class);

    public HandlerFinder(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.gson = new Gson();
    }

    public void findHandler() throws IOException {
        List<Class<?>> handlerClasses = BeanLoader.getHandlerClasses();
        for (Class<?> handlerClass : handlerClasses) {
            String requestPath = request.getPath();
            String handlerPath = handlerClass.getDeclaredAnnotation(Handler.class).value();
            if (!handlerPath.startsWith("/")) handlerPath = "/" + handlerPath;

            if (requestPath.startsWith(handlerPath)) {
                int index = requestPath.indexOf(handlerPath);
                requestPath = requestPath.substring(index + handlerPath.length());
            }

            try {
                Method handlerMethod = findHandleMethod(handlerClass, requestPath);
                Object[] parameters = getHandlerMethodParameters(handlerMethod.getParameters()).toArray();
                Object returnValue = handlerMethod.invoke(handlerClass.newInstance(), parameters);
                String json = gson.toJson(returnValue);
                response.pass(json, HttpStatus.OK);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NotFoundHandlerException e) {
                e.printStackTrace();
                notFoundHandler();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                badRequest();
            }
        }
    }

    private Method findHandleMethod(Class<?> handlerClass, String requestPath) throws NotFoundHandlerException {
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Annotation[] declaredAnnotations = declaredMethod.getDeclaredAnnotations();
            for (Annotation declaredAnnotation : declaredAnnotations) {
                for (Class<? extends Annotation> handleAnnotation : handleAnnotations) {
                    if (handleAnnotation.equals(declaredAnnotation.annotationType())) {
                        Method pathValue;
                        Method methodValue;
                        try {
                            pathValue = handleAnnotation.getDeclaredMethod("value");
                            methodValue = handleAnnotation.getDeclaredMethod("method");
                            String path = pathValue.invoke(declaredAnnotation).toString();
                            String method = methodValue.invoke(declaredAnnotation).toString();

                            if (requestPath.equals(path) && request.getMethod().equals(HttpMethod.get(method))) {
                                if (declaredMethod.getDeclaredAnnotation(RestApi.class) != null) {
                                    this.response.setContentMimeType(ContentType.JSON);
                                }
                                return declaredMethod;
                            }
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
        throw new NotFoundHandlerException();
    }

    private List<Object> getHandlerMethodParameters(Parameter[] parameters) {
        List<Object> params = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            String value = request.getParameter(name);

            Class<?> type = parameter.getType();
            if (value != null) {
                if (type.isPrimitive()) {
                    Object autoBoxingValue = PrimitiveWrapper.wrapPrimitiveValue(type, value);
                    params.add(autoBoxingValue);
                } else if (type.equals(String.class)) {
                    params.add(value);
                }
            } else {
                Object object = Converter.parameterToObject(request.getParameters(), type);
                params.add(object);
            }
        }
        return params;
    }

    private void notFoundHandler() {
        this.response.fileNotFound();
    }

    private void badRequest() {
        this.response.badRequest();
    }

}
