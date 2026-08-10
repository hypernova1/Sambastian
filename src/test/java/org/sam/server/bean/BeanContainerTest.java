package org.sam.server.bean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BeanContainerTest {

    private final TestService testService = new TestService();

    @BeforeEach
    void 컨테이너_초기화() {
        Map<Class<?>, List<BeanDefinition>> beanDefinitionMap = new HashMap<>();
        List<BeanDefinition> definitions = new ArrayList<>();
        definitions.add(BeanDefinition.of("testService", testService));
        beanDefinitionMap.put(TestService.class, definitions);
        BeanContainer.load(beanDefinitionMap, new ArrayList<>(), new ArrayList<>());
    }

    @Test
    @DisplayName("getBean은 BeanDefinition이 아닌 실제 빈 인스턴스를 반환한다")
    void getBean_인스턴스_반환() {
        TestService bean = BeanContainer.getInstance().getBean("testService", TestService.class);

        assertSame(testService, bean);
    }

    @Test
    @DisplayName("등록되지 않은 타입이나 이름을 조회하면 NPE 없이 null을 반환한다")
    void getBean_미등록_조회() {
        BeanContainer container = BeanContainer.getInstance();

        assertNull(container.getBean("none", String.class));
        assertNull(container.getBean("wrongName", TestService.class));
    }

    @Test
    @DisplayName("registerBean으로 신규 타입 빈을 등록하면 getBean으로 조회할 수 있다")
    void registerBean_신규_타입_등록() {
        BeanContainer container = BeanContainer.getInstance();
        AnotherService another = new AnotherService();

        container.registerBean("anotherService", another);

        AnotherService bean = container.getBean("anotherService", AnotherService.class);
        assertSame(another, bean);
    }

    @Test
    @DisplayName("같은 이름으로 중복 등록하면 무시된다")
    void registerBean_중복_무시() {
        BeanContainer container = BeanContainer.getInstance();
        container.registerBean("dup", new AnotherService());
        container.registerBean("dup", new AnotherService());

        assertEquals(1, container.getBeanDefinitionList(AnotherService.class).size());
    }

    @Test
    @DisplayName("인터페이스 타입으로 구현체 빈 목록을 조회할 수 있다")
    void getBeanList_인터페이스_조회() {
        List<?> beans = BeanContainer.getInstance().getBeanList(TestInterface.class);

        assertEquals(1, beans.size());
        assertSame(testService, beans.get(0));
    }

    @Test
    @DisplayName("매칭되는 빈이 없으면 NPE 없이 빈 목록을 반환한다")
    void getBeanList_미등록_타입() {
        List<?> beans = BeanContainer.getInstance().getBeanList(Runnable.class);

        assertTrue(beans.isEmpty());
    }

    interface TestInterface {
    }

    static class TestService implements TestInterface {
    }

    static class AnotherService {
    }

}
