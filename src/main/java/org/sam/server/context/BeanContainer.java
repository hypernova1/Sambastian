package org.sam.server.context;

import org.sam.server.annotation.Qualifier;
import org.sam.server.annotation.component.Bean;
import org.sam.server.http.Interceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 빈을 생성하고 관리하는 클래스
 *
 * @author hypernova1
 */
public class BeanContainer {

    private static final Logger logger = LoggerFactory.getLogger(BeanContainer.class);

    private final Map<Class<?>, List<BeanDefinition>> beanDefinitionMap = new HashMap<>();

    private final List<Object> handlerBeans = new ArrayList<>();

    private final List<Interceptor> interceptors = new ArrayList<>();

    private final BeanClassLoader beanClassLoader;

    private BeanContainer(BeanClassLoader beanClassLoader) {
        this.beanClassLoader = beanClassLoader;

        loadBeans();
        loadHandler();
        loadInterceptors();
    }

    /**
     * 컴포넌트 클래스의 인스턴스를 생성하고 저장한다.
     */
    private void loadBeans() {
        List<Class<?>> noParameterComponents = new ArrayList<>();
        List<Class<?>> parameterComponents = new ArrayList<>();
        for (Class<?> componentClass : beanClassLoader.getComponentClasses()) {
            try {
                componentClass.getConstructor();
                noParameterComponents.add(componentClass);
            } catch (NoSuchMethodException e) {
                parameterComponents.add(componentClass);
            }
        }

        createNoParameterBeans(noParameterComponents);
        createParameterBeans(parameterComponents);
    }

    /**
     * 생성자에 파라미터가 클래스빈을 생성한다.
     */
    private void createNoParameterBeans(List<Class<?>> componentClasses) {
        for (Class<?> componentClass : componentClasses) {
            this.createClassBeanWithDeclaredMethodBeans(componentClass);
            this.beanClassLoader.getComponentClasses().remove(componentClass);
        }
    }

    /**
     * 생성자에 파라미터가 있는 클래스 빈을 생성한다.
     */
    private void createParameterBeans(List<Class<?>> componentClasses) {
        List<Class<?>> circularReferences = new ArrayList<>();
        while (!componentClasses.isEmpty()) {
            Iterator<Class<?>> iterator = componentClasses.iterator();
            while (iterator.hasNext()) {
                Class<?> componentClass = iterator.next();
                if (existsBean(componentClass)) {
                    continue;
                }

                boolean isCreated = this.createClassBeanWithDeclaredMethodBeans(componentClass);
                if (!isCreated) {
                    checkCircularReferences(circularReferences, componentClass);
                    continue;
                }

                iterator.remove();
                circularReferences.clear();
            }
        }
    }

    /**
     * 순환 참조 여부를 확인한다.
     * */
    private static void checkCircularReferences(List<Class<?>> circularReferences, Class<?> componentClass) {
        circularReferences.add(componentClass);
        if (circularReferences.size() == 1) {
            return;
        }

        boolean isCircularReferences = circularReferences.get(0) == circularReferences.get(circularReferences.size() - 1);
        if (!isCircularReferences) {
            return;
        }

        String circularReferenceInfo = circularReferences.stream().map(Class::getName).collect(Collectors.joining(" -> "));
        throw new CircularReferenceException(circularReferenceInfo);
    }

    /**
     * 클래스 빈을 생성한다. 내부 메서드에 @Bean 이 선언되어 있으면 해당 메서드의 반환 값을 빈으로 생성한다.
     */
    private boolean createClassBeanWithDeclaredMethodBeans(Class<?> componentClass) {
        Object instance = createComponentInstanceOrNull(componentClass);
        if (instance == null) {
            return false;
        }
        addBeanMap(componentClass, instance, getBeanName(componentClass));

        this.createMethodBean(instance);
        return true;
    }

    /**
     * 클래스에 선언되어 있는 메서드 빈을 생성한다.
     */
    private void createMethodBean(Object componentInstance) {
        assert componentInstance != null;
        Method[] methods = componentInstance.getClass().getMethods();
        loadMethodBean(componentInstance, methods);
    }

    /**
     * 컴포넌트 클래스 내부의 빈 메서드의 결과값을 받아 컴포넌트 인스턴스 목록에 추가한다.
     *
     * @param componentInstance 컴포넌트 인스턴스
     * @param declaredMethods   컴포넌트 클래스에 선언 된 메서드 목록
     */
    private void loadMethodBean(Object componentInstance, Method[] declaredMethods) {
        for (Method declaredMethod : declaredMethods) {
            if (declaredMethod.getDeclaredAnnotation(Bean.class) == null) {
                continue;
            }

            try {
                Class<?> beanType = declaredMethod.getReturnType();
                Object instance = declaredMethod.invoke(componentInstance);
                String beanName = declaredMethod.getName();
                addBeanMap(beanType, instance, beanName);
            } catch (IllegalAccessException e) {
                throw new BeanAccessModifierException();
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * 생성된 빈을 저장한다.
     *
     * @param componentType     컴포넌트 타입
     * @param componentInstance 컴포넌트 인스턴스
     * @param beanName          빈 이름
     */
    private void addBeanMap(Class<?> componentType, Object componentInstance, String beanName) {
        BeanDefinition beanDefinition = BeanDefinition.of(beanName, componentInstance);

        if (beanDefinitionMap.get(componentType) != null) {
            beanDefinitionMap.get(componentType).add(beanDefinition);
            return;
        }
        List<BeanDefinition> beanDefinitionList = new ArrayList<>();
        beanDefinitionList.add(beanDefinition);
        beanDefinitionMap.put(componentType, beanDefinitionList);

        logger.info("create bean: {} > {}", beanName, componentType.getName());
    }

    /**
     * 핸들러 클래스의 인스턴스를 생성하고 저장한다.
     */
    private void loadHandler() {
        for (Class<?> handlerClass : beanClassLoader.getHandlerClasses()) {
            Object bean = createComponentInstanceOrNull(handlerClass);
            logger.info("create handler bean: {}", handlerClass.getName());
            handlerBeans.add(bean);
        }
    }

    /**
     * 인터셉터 구현체 클래스의 인스턴스를 생성하고 저장한다.
     */
    private void loadInterceptors() {
        try {
            for (Class<?> interceptorClass : beanClassLoader.getInterceptorClasses()) {
                Interceptor interceptor = (Interceptor) interceptorClass.getDeclaredConstructor().newInstance();
                interceptors.add(interceptor);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 컴포넌트의 인스턴스를 생성 후 반환한다.
     *
     * @param clazz 클래스 타입
     * @return 컴포넌트 인스턴스
     */
    private Object createComponentInstanceOrNull(Class<?> clazz) {
        Constructor<?>[] constructors = clazz.getConstructors();
        try {
            Constructor<?> constructor = getDefaultConstructor(clazz, constructors);
            Parameter[] constructorParameters = constructor.getParameters();
            List<Object> parameters = getParametersOrNull(constructorParameters);
            if (parameters == null) {
                return null;
            }

            int differenceParameterNumber = constructorParameters.length - parameters.size();
            if (differenceParameterNumber < 0) {
                throw new BeanCreationException(clazz);
            }

            if (differenceParameterNumber > 0) {
                for (int i = 0; i < differenceParameterNumber; i++) {
                    parameters.add(null);
                }
            }

            return constructor.newInstance(parameters.toArray());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new BeanCreationException(clazz);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 클래스 빈에 주입될 의존성 파라미터를 가져온다.
     */
    private List<Object> getParametersOrNull(Parameter[] parameters) {
        List<Object> parameterList = new ArrayList<>();
        for (Parameter parameter : parameters) {
            try {
                BeanDefinition beanDefinition = this.getBeanDefinition(parameter);
                if (beanDefinition == null) {
                    return null;
                }
                parameterList.add(beanDefinition.getBeanInstance());
            } catch (BeanNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        return parameterList;
    }

    /**
     * 클래스 빈의 생성자에 주입될 빈을 가져온다.
     */
    private BeanDefinition getBeanDefinition(Parameter parameter) {
        String parameterName = parameter.getName();
        return findBeanDefinition(parameter.getType(), parameterName);
    }

    /**
     * 빈 이름을 생성 후 반환한다.
     *
     * @param componentType 컴포넌트 타입
     * @return 빈 이름
     */
    private String getBeanName(Class<?> componentType) {
        Qualifier qualifier = componentType.getDeclaredAnnotation(Qualifier.class);
        if (qualifier != null) {
            return qualifier.value();
        }
        String beanName = componentType.getSimpleName();
        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
    }

    /**
     * 존재하는 빈이 있는 지 확인한다.
     *
     * @param componentType 컴포넌트 타입
     * @return 중복 유무
     */
    private boolean existsBean(Class<?> componentType) {
        String beanName = getBeanName(componentType);
        List<BeanDefinition> beanDefinitions = this.beanDefinitionMap.get(componentType);
        if (beanDefinitions == null) {
            return false;
        }

        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinition.getBeanName().equals(beanName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 빈이 있는지 확인 후 없다면 생성하고 정보를 반환한다.
     *
     * @param componentType 컴포넌트 타입
     * @param parameterName 파라미터 이름
     * @return 빈 정보
     */
    private BeanDefinition findBeanDefinition(Class<?> componentType, String parameterName) {
        if (!this.beanClassLoader.getComponentClasses().contains(componentType)) {
            componentType = findSuperClass(componentType);
        }

        List<BeanDefinition> beanDefinitions = this.beanDefinitionMap.get(componentType);
        if (beanDefinitions == null) {
            return null;
        }

        if (beanDefinitions.size() == 1) {
            return beanDefinitions.get(0);
        }

        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (!beanDefinition.getBeanName().equals(parameterName)) {
                continue;
            }

            return beanDefinition;
        }
        return null;
    }

    /**
     * 컴포넌트 타입에 해당하는 빈이 존재하는 지 확인 후 있다면 키를 반환한다.
     *
     * @param componentType 컴포넌트 타입
     * @return 빈 키
     * <p>
     * TODO: 여러 구현체 있을 때 대응
     */
    private Class<?> findSuperClass(Class<?> componentType) {
        Set<Class<?>> keys = this.beanDefinitionMap.keySet();
        for (Class<?> key : keys) {
            if (!componentType.isAssignableFrom(key)) continue;
            return key;
        }
        return null;
    }

    /**
     * 기본 생성자를 반환한다.
     *
     * @param clazz        클래스 타입
     * @param constructors 생성자 목록
     * @return 기본 생성자
     */
    private Constructor<?> getDefaultConstructor(Class<?> clazz, Constructor<?>[] constructors) {
        if (constructors.length > 0) {
            return constructors[0];
        }

        try {
            return clazz.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 핸들러 빈 목록을 반환한다.
     *
     * @return 핸들러 빈 목록
     */
    public List<Object> getHandlerBeans() {
        return this.handlerBeans;
    }

    public Object findHandlerByClass(Class<?> clazz) {
        return this.handlerBeans.stream().filter((b) -> b.getClass().equals(clazz)).findFirst().orElse(null);
    }

    /**
     * 인터셉터 구현체 인스턴스 목록을 반환한다.
     *
     * @return 인터셉터 구현체 인스턴스
     */
    public List<Interceptor> getInterceptors() {
        return this.interceptors;
    }

    /**
     * 빈 목록을 반환한다.
     *
     * @return 빈 목록
     */
    public Map<Class<?>, List<BeanDefinition>> getBeanDefinitionMap() {
        return this.beanDefinitionMap;
    }

    public List<BeanDefinition> getBeanDefinitionList(Class<?> type) {
        return this.beanDefinitionMap.get(type);
    }

    private static class BeanContainerHolder {
        public static final BeanContainer INSTANCE = new BeanContainer(BeanClassLoader.getInstance());
    }

    public static BeanContainer getInstance() {
        return BeanContainerHolder.INSTANCE;
    }
}
