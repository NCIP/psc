package org.dynamicjava.api_bridge;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.dynamicjava.api_bridge.exceptions.ApiBridgeException;
import org.dynamicjava.api_bridge.utilities.ClassUtils;

public class Delegator implements InvocationHandler {
	
	//@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Method delegateMethod = findDelegateMethod(method);
		if (delegateMethod != null) {
			return getApiBridge().bridge(delegateMethod.invoke(getDelegate(), bridgeObjects(args)), true);
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
