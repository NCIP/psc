package org.dynamicjava.api_bridge;

import org.dynamicjava.api_bridge.exceptions.ApiBridgeException;
import org.dynamicjava.api_bridge.utilities.ClassUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Delegator extends AbstractDelegator implements InvocationHandler {

    //@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method delegateMethod = findDelegateMethod(method);
		if (delegateMethod != null) {
            log.debug("Bridging method {} with args {}", method, args == null ? "<none>" : Arrays.asList(args));
            log.debug("Delegate method is {} from {}", delegateMethod, delegateMethod.getDeclaringClass());
            try {
                return getApiBridge().bridge(delegateMethod.invoke(getDelegate(), bridgeObjects(args, delegateMethod.getParameterTypes())), true);
            } catch (InvocationTargetException ite) {
                throw (Throwable) getApiBridge().bridge(ite.getTargetException(), true);
            }
		} else {
			throw new ApiBridgeException(String.format(
					"Method '%s' was not found in the delegate object", method.getName()));
		}
	}

	
	protected Method findDelegateMethod(Method method) {
		Method result = getApiBridge().getCache().getMethodMap().get(method);
		if (result != null) {
			return result;
		} else {
			result = ClassUtils.findMethod(getDelegate().getClass(),
					method.getName(), method.getParameterTypes());
			getApiBridge().getCache().getMethodMap().put(method, result);
			return result;
		}
	}

    public Delegator(Object delegate, ApiBridge apiBridge) {
        super(delegate, apiBridge);
    }

}
