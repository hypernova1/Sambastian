//package org.sam.server.context;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.sam.server.annotation.component.Component;
//import org.sam.server.http.Interceptor;
//import org.sam.server.http.web.request.Request;
//import org.sam.server.http.web.response.Response;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class BeanLoaderTest {
//
//    private BeanLoader beanLoader;
//    private BeanClassLoader beanClassLoader;
//
//    @BeforeEach
//    void setUp() {
//        beanLoader = BeanLoader.getInstance();
//        beanClassLoader = BeanClassLoader.getInstance();
//    }
//
//    @Test
//    void testLoadBeans() {
//        Map<Class<?>, List<BeanDefinition>> beanDefinitionMap = beanLoader.getBeanDefinitionMap();
//        assertNotNull(beanDefinitionMap.get(TestComponent.class));
//        assertEquals(1, beanDefinitionMap.get(TestComponent.class).size());
//    }
//
//    @Test
//    void testLoadHandler() {
//        List<Object> handlerBeans = beanLoader.getHandlerBeans();
//        assertEquals(1, handlerBeans.size());
//        assertTrue(handlerBeans.get(0) instanceof TestHandler);
//    }
//
//    @Test
//    void testLoadInterceptors() {
//        List<Interceptor> interceptors = beanLoader.getInterceptors();
//        assertEquals(1, interceptors.size());
//        assertTrue(interceptors.get(0) instanceof TestInterceptor);
//    }
//
//    @Test
//    void testCircularReference() {
//        Exception exception = assertThrows(CircularReferenceException.class, () -> {
//            beanLoader.createParameterBeans(Collections.singletonList(CircularReferenceComponentA.class));
//        });
//        String expectedMessage = "org.sam.server.context.CircularReferenceComponentA -> org.sam.server.context.CircularReferenceComponentB -> org.sam.server.context.CircularReferenceComponentA";
//        String actualMessage = exception.getMessage();
//        assertTrue(actualMessage.contains(expectedMessage));
//    }
//
//    static class TestComponent {
//    }
//
//    static class TestComponentWithParams {
//        public TestComponentWithParams(TestComponent testComponent) {
//        }
//    }
//
//    static class TestHandler {
//    }
//
//    static class TestInterceptor implements Interceptor {
//        @Override
//        public void preHandler(Request request, Response response) {
//
//        }
//
//        @Override
//        public void postHandler(Request request, Response response) {
//
//        }
//    }
//
//    @Component
//    static class CircularReferenceComponentA {
//        public CircularReferenceComponentA(CircularReferenceComponentB componentB) {
//        }
//    }
//
//    @Component
//    static class CircularReferenceComponentB {
//        public CircularReferenceComponentB(CircularReferenceComponentA componentA) {
//        }
//    }
//}