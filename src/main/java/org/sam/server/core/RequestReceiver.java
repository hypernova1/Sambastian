package org.sam.server.core;

import com.google.gson.Gson;
import org.sam.server.annotation.handle.*;
import org.sam.server.common.PrimitiveWrapper;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpStatus;
import org.sam.server.exception.NotFoundHandlerException;
import org.sam.server.http.Request;
import org.sam.server.http.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by melchor
 * Date: 2020/07/17
 * Time: 1:34 PM
 */
public class RequestReceiver {

    private final Socket connect;

    private Request request;
    private Response response;

    Gson gson = new Gson();

    private List<Class<? extends Annotation>> handleAnnotations =
            Arrays.asList(GetHandle.class, PostHandle.class, PutHandle.class, DeleteHandle.class);

    public RequestReceiver(Socket connect) {
        this.connect = connect;
    }

    public void requestAnalyze() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream(), StandardCharsets.UTF_8))) {

            this.request = Request.create(in);
            this.response = Response.create(connect.getOutputStream(), request.getPath());
            executeHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeHandler() throws IOException {
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
                Method handlerMethod = findMethod(handlerClass, requestPath);
                Object[] parameters = getHandlerMethodParameters(handlerMethod.getParameters()).toArray();
                Object returnValue = handlerMethod.invoke(handlerClass.newInstance(), parameters);
                String json = gson.toJson(returnValue);
                response.pass(json, HttpStatus.OK);
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NotFoundHandlerException e) {
                e.printStackTrace();
                notFoundHandler();
            } catch (IllegalArgumentException e) {
                badRequest();
            }
            finally {
                connect.close();
            }
        }
    }

    private List<Object> getHandlerMethodParameters(Parameter[] parameters) {
        List<Object> params = new ArrayList<>();
        for (Parameter parameter : parameters) {
            String name = parameter.getName();
            String value = request.getParameter(name);

            if (value != null) {
                Class<?> type = parameter.getType();
                if (type.isPrimitive()) {
                    Object autoBoxingValue = PrimitiveWrapper.wrapPrimitiveValue(type, value);
                    params.add(autoBoxingValue);
                } else if (type.equals(String.class)) {
                    params.add(value);
                } else {
                    params.add(value);
                }

            }
        };

        return params;
    }

    private Method findMethod(Class<?> handlerClass, String requestPath) throws NotFoundHandlerException {
        Method[] declaredMethods = handlerClass.getDeclaredMethods();
        for (Method declaredMethod : declaredMethods) {
            Annotation[] declaredAnnotations = declaredMethod.getDeclaredAnnotations();
            for (Annotation declaredAnnotation : declaredAnnotations) {
                for (Class<? extends Annotation> handleAnnotation : handleAnnotations) {
                    if (handleAnnotation.equals(declaredAnnotation.annotationType())) {
                        Method method;
                        try {
                            method = handleAnnotation.getDeclaredMethod("value");
                            Object path = method.invoke(declaredAnnotation);

                            if (requestPath.equals(path)) {
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

    private void notFoundHandler() {
        this.response.fileNotFound();
    }

    private void badRequest() {
        this.response.badRequest();
    }

}
