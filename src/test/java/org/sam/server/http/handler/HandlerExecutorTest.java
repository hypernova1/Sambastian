package org.sam.server.http.handler;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sam.server.annotation.ExceptionResponse;
import org.sam.server.bean.BeanContainer;
import org.sam.server.bean.Handler;
import org.sam.server.constant.ContentType;
import org.sam.server.constant.HttpMethod;
import org.sam.server.constant.HttpStatus;
import org.sam.server.http.Cookie;
import org.sam.server.http.Session;
import org.sam.server.http.web.HttpExceptionHandler;
import org.sam.server.http.web.request.Request;
import org.sam.server.http.web.response.Response;
import org.sam.server.http.web.response.ResponseEntity;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HandlerExecutorTest {

    @BeforeAll
    static void 빈_컨테이너_초기화() {
        List<Object> handlerBeans = new ArrayList<>();
        handlerBeans.add(new TestExceptionHandler());
        BeanContainer.load(new HashMap<>(), handlerBeans, new ArrayList<>());
    }

    @Test
    @DisplayName("핸들러 메서드를 인스턴스 대상으로 호출하고 반환 값을 JSON으로 응답한다")
    void 핸들러_정상_실행() throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getMethod("hello");
        FakeResponse response = new FakeResponse();

        HandlerExecutor.getInstance().execute(Handler.of(controller, method), new FakeRequest(), response);

        assertEquals(HttpStatus.OK, response.executedStatus);
        assertTrue(response.executedBody.contains("hello"));
    }

    @Test
    @DisplayName("핸들러에서 예외가 발생하면 NPE 없이 500으로 응답한다")
    void 핸들러_예외_발생() throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getMethod("fail");
        FakeResponse response = new FakeResponse();

        HandlerExecutor.getInstance().execute(Handler.of(controller, method), new FakeRequest(), response);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.executedStatus);
    }

    @Test
    @DisplayName("@ExceptionResponse에 등록된 예외는 해당 핸들러가 처리한다")
    void 예외_핸들러_매칭() throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method = TestController.class.getMethod("failWithCustomException");
        FakeResponse response = new FakeResponse();

        HandlerExecutor.getInstance().execute(Handler.of(controller, method), new FakeRequest(), response);

        assertEquals(HttpStatus.BAD_REQUEST, response.executedStatus);
        assertTrue(response.executedBody.contains("custom-handled"));
    }

    @Test
    @DisplayName("notFound()는 404 상태를 가진다")
    void notFound_상태코드() {
        assertEquals(HttpStatus.NOT_FOUND, ResponseEntity.notFound().getHttpStatus());
    }

    public static class TestController {
        public String hello() {
            return "hello";
        }

        public String fail() {
            throw new IllegalStateException("boom");
        }

        public String failWithCustomException() {
            throw new CustomException();
        }
    }

    public static class CustomException extends RuntimeException {
    }

    public static class TestExceptionHandler implements HttpExceptionHandler {
        @ExceptionResponse(CustomException.class)
        public ResponseEntity<String> handleCustom(CustomException e) {
            return ResponseEntity.of(HttpStatus.BAD_REQUEST, "custom-handled");
        }

        // @ExceptionResponse가 없는 일반 메서드가 있어도 NPE가 나지 않아야 한다
        public void normalMethod() {
        }
    }

    static class FakeRequest implements Request {
        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public String getUrl() {
            return "/test";
        }

        @Override
        public HttpMethod getMethod() {
            return HttpMethod.GET;
        }

        @Override
        public String getParameter(String key) {
            return null;
        }

        @Override
        public Map<String, String> getParameters() {
            return new HashMap<>();
        }

        @Override
        public Set<String> getParameterNames() {
            return new HashSet<>();
        }

        @Override
        public Set<String> getHeaderNames() {
            return new HashSet<>();
        }

        @Override
        public String getHeader(String key) {
            return null;
        }

        @Override
        public String getJson() {
            return null;
        }

        @Override
        public Set<Cookie> getCookies() {
            return new HashSet<>();
        }

        @Override
        public Session getSession() {
            return null;
        }

        @Override
        public boolean isFaviconRequest() {
            return false;
        }

        @Override
        public boolean isResourceRequest() {
            return false;
        }

        @Override
        public boolean isOptionsRequest() {
            return false;
        }

        @Override
        public boolean isRootRequest() {
            return false;
        }
    }

    static class FakeResponse implements Response {
        String executedBody;
        HttpStatus executedStatus;
        final Map<String, String> headers = new HashMap<>();

        @Override
        public void execute(String pathOrJson, HttpStatus status) {
            this.executedBody = pathOrJson;
            this.executedStatus = status;
        }

        @Override
        public void favicon() {
        }

        @Override
        public void indexFile() {
        }

        @Override
        public void staticResources() {
        }

        @Override
        public void notFound() {
            this.executedStatus = HttpStatus.NOT_FOUND;
        }

        @Override
        public void badRequest() {
            this.executedStatus = HttpStatus.BAD_REQUEST;
        }

        @Override
        public void methodNotAllowed() {
            this.executedStatus = HttpStatus.METHOD_NOT_ALLOWED;
        }

        @Override
        public void allowedMethods() {
        }

        @Override
        public void setHeader(String key, String value) {
            headers.put(key, value);
        }

        @Override
        public void setContentMimeType(ContentType contentMimeType) {
        }

        @Override
        public void addAllowedMethod(HttpMethod httpMethod) {
        }

        @Override
        public void addCookies(Cookie cookie) {
        }

        @Override
        public Object getHeader(String key) {
            return headers.get(key);
        }

        @Override
        public Set<String> getHeaderNames() {
            return headers.keySet();
        }
    }

}
