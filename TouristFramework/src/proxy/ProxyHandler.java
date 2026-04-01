package proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import aop.AopConfigLoader;

public class ProxyHandler implements InvocationHandler {
    private final Object target;

    public ProxyHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        AopConfigLoader.execute(target, method.getName(), "before");

        Object result = method.invoke(target, args);

        AopConfigLoader.execute(target, method.getName(), "after");

        return result;
    }
}