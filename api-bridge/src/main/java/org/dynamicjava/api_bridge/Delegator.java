package org.dynamicjava.api_bridge;

import org.dynamicjava.api_bridge.exceptions.ApiBridgeException;
import org.dynamicjava.api_bridge.utilities.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Delegator implements InvocationHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
	
	//@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method delegateMethod = findDelegateMethod(method);
		if (delegateMethod != null) {
            log.debug("Bridging method {} with args {}", method, args == null ? "<none>" : Arrays.asList(args));
            log.debug("Delegate method is {} from {}", delegateMethod, delegateMethod.getDeclaringClass());
            try {
                return getApiBridge().bridge(delegateMethod.invoke(getDelegate(), bridgeObjects(args)), true);
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
	
	protected Object[] bridgeObjects(Object[] args) {
		if (args == null) {
			return null;
		}
		
		List<Object> result = new ArrayList<Object>();
		for (Object arg : args) {
			result.add(getDelegateApiBridge().bridge(arg, true));
		}
		return result.toArray();
	}
	
	
	public Delegator(Object delegate, ApiBridge apiBridge) {
		this.delegate = delegate;
		this.apiBridge = apiBridge;
		this.delegateApiBridge = ApiBridge.getApiBridge(
				ApiBridgeClassLoader.getClassLoader(apiBridge.getTargetApiClassLoader(),
						delegate.getClass().getClassLoader(), apiBridge.getApiPackageNamesArray()),
						apiBridge.getApiPackageNamesArray());
	}
	
	private final Object delegate;
	protected Object getDelegate() {
		return delegate;
	}
	
	private final ApiBridge apiBridge;
	protected ApiBridge getApiBridge() {
		return apiBridge;
	}
	
	private final ApiBridge delegateApiBridge;
	protected ApiBridge getDelegateApiBridge() {
		return delegateApiBridge;
	}
	
}
