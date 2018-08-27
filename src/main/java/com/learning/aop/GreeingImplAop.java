package com.learning.aop;

import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;

/**
 * @Description:
 * @author: chengan.liang
 * @Date: 2018/6/29
 */
public class GreeingImplAop {


    /**
     * step1:
     * 在没有代理之前，如果想在方法执行前后执行其他的代码，只能写死在每个
     * 方法里面，如下，如果很多个方法需要执行相同的方法就会很臃肿且繁琐
     */
    static class GreetingImplStatic implements Greeting {

        @Override
        public void sayHello(String name) {
            before();
            System.out.println(String.format("Hello %S", name));
            after();
        }

        public void before() {
            System.out.println("before method....");
        }

        public void after() {
            System.out.println("after method....");
        }


        public static void main(String[] args) {
            GreetingImplStatic greetingImplStatic = new GreetingImplStatic();
            greetingImplStatic.sayHello("john");
        }
    }

    static class GreetingImpl implements Greeting {
        @Override
        public void sayHello(String name) {
            System.out.println(String.format("Hello %S", name));
        }
    }


    /**
     * step2：
     * 解决step1中的问题最简单的方法就是静态代理类,如下
     * 静态代理类造成的问题：
     * 随着这样的代码越来越多，xxxxProxy类会越来越多
     */
    static class GreetingProxy implements Greeting {

        private GreetingImpl greetingImpl;

        public GreetingProxy(GreetingImpl greetingImpl) {
            this.greetingImpl = greetingImpl;
        }

        @Override
        public void sayHello(String name) {
            before();
            greetingImpl.sayHello(name);
            after();
        }

        public void before() {
            System.out.println("before method....");
        }

        public void after() {
            System.out.println("after method....");
        }


        public static void main(String[] args) {
            GreetingProxy greetingProxy = new GreetingProxy(new GreetingImpl());
            greetingProxy.sayHello("JOHN");
        }

    }

    /**
     * step3:
     * 为了解决step2中的问题，就需要用到JDK的动态代理了，如下：
     * JDK动态代理带来的问题：
     * JDK提供的动态代理只能代理接口，而不能代理没有接口的类
     */
    static class GreetingJdkDynamicProxy implements InvocationHandler {

        private Object target;

        public GreetingJdkDynamicProxy(Object target) {
            this.target = target;
        }

        @SuppressWarnings("unchecked")
        public <T> T getProxy() {
            return (T) Proxy.newProxyInstance(
                    target.getClass().getClassLoader(),
                    target.getClass().getInterfaces(),
                    this
            );
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            before();
            Object result = method.invoke(target, args);
            after();
            return result;
        }

        public void before() {
            System.out.println("before method....");
        }

        public void after() {
            System.out.println("after method....");
        }

        public static void main(String[] args) {
            Greeting greeting = new GreetingJdkDynamicProxy(new GreetingImpl()).getProxy();
            greeting.sayHello("JDK");
        }
    }

    /**
     * step4:
     * 为了弥补JDK提供的动态的代理的不足，可以使用开源的CGLIB类库，如下：
     */
    static class GreetingCGLIB implements MethodInterceptor {

        private static GreetingCGLIB instance = new GreetingCGLIB();

        public GreetingCGLIB() {
        }

        public static GreetingCGLIB getInstance() {
            return instance;
        }

        @SuppressWarnings("unchecked")
        public <T> T getProxy(Class<T> cls) {
            return (T) Enhancer.create(cls, this);
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            before();
            Object result = methodProxy.invokeSuper(o, objects);
            after();
            return result;
        }

        public void before() {
            System.out.println("before method....");
        }

        public void after() {
            System.out.println("after method....");
        }

        public static void main(String[] args) {
            GreetingImpl proxy = GreetingCGLIB.getInstance().getProxy(GreetingImpl.class);

            proxy.sayHello("CGLIB");
        }
    }

    /**
     * step5:
     * 以上就是cglib的实现，spring中的aop就是利用了上面的思想对代码进行增强，
     * before就是相当于前置增强，after就是后置增强，spring aop的做法如下：
     */
    static class GreetingBefore implements MethodBeforeAdvice {
        @Override
        public void before(Method method, Object[] objects, Object o) throws Throwable {
            System.out.println("aop before");
        }
    }

    static class GreetingAfter implements AfterReturningAdvice {
        @Override
        public void afterReturning(Object o, Method method, Object[] objects, Object o1) throws Throwable {
            System.out.println("aop after");
        }
    }

    public static void main(String[] args) {
        ProxyFactory proxyFactory = new ProxyFactory();
        proxyFactory.setTarget(new GreetingImpl());
        proxyFactory.addAdvice(new GreetingBefore());
        proxyFactory.addAdvice(new GreetingAfter());

        Greeting proxy = (Greeting) proxyFactory.getProxy();
        proxy.sayHello("AOP");
    }




}



