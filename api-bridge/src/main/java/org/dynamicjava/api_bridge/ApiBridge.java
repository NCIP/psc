package org.dynamicjava.api_bridge;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import net.sf.cglib.proxy.NoOp;
import org.dynamicjava.api_bridge.exceptions.ApiBridgeException;
import org.dynamicjava.api_bridge.exceptions.UntranslatableClassException;
import org.dynamicjava.api_bridge.utilities.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ApiBridge {
    private final Logger log = LoggerFactory.getLogger(getClass());
	
	public Object bridge(Object apiObject) {
		return bridge(apiObject, false);
	}
	
	public Object bridge(Object apiObject, boolean returnSameObjectIfUnbridgable) {
		if (apiObject == null) {
            log.debug("Cannot bridge null value");
			return null;
		}
        log.debug("Bridging {} ({}@{})", new Object[] { apiObject, apiObject.getClass().getName(), Integer.toHexString(System.identityHashCode(apiObject)) });

		if (apiObject.getClass().isArray()) {
			if (isOfApiPackages(apiObject.getClass().getComponentType())) {
				int arrayLength = Array.getLength(apiObject);
				Class<?> matchingComponentType = findClassMatch(apiObject.getClass().getComponentType());
				Object matchingArray = Array.newInstance(matchingComponentType, arrayLength);
				for (int i = 0; i < arrayLength; i++) {
					Array.set(matchingArray, i, bridge(Array.get(apiObject, i)));
				}
				return matchingArray;
			} else {
                return handleUnbridgableObject(apiObject, returnSameObjectIfUnbridgable);
            }
		}
		
		Class<?> superClassMatch = findSuperClassMatch(apiObject.getClass());
		if (superClassMatch != null) {
			return createProxyForSuperClass(apiObject, superClassMatch);
		} else {
			Set<Class<?>> interfacesMatch = findIntefacesMatch(apiObject.getClass());
			if (interfacesMatch.size() > 0) {
				return createProxyForInterfaces(apiObject, interfacesMatch.toArray(new Class<?>[0]));
			} else {
                return handleUnbridgableObject(apiObject, returnSameObjectIfUnbridgable);
            }
		}
	}

    private Object handleUnbridgableObject(Object apiObject, boolean returnSameObjectIfUnbridgable) {
        if (apiObject instanceof Collection) {
            return new DelegatorCollection((Collection) apiObject, this);
        } else if (returnSameObjectIfUnbridgable) {
            log.debug("Cannot bridge; returning original object");
            return apiObject;
        } else {
            throw new UntranslatableClassException(apiObject.getClass());
        }
    }

    protected Object createProxyForSuperClass(Object apiObject, Class<?> superClassMatch) {
		final Delegator delegator = new Delegator(apiObject, this);
		MethodInterceptor invocationHandler = new MethodInterceptor() {
			//@Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            	return delegator.invoke(obj, method, args);
            }
        };

        Set<Class<?>> interfaces = findIntefacesMatch(apiObject.getClass());

        log.debug("Creating proxy for superclass {} of {}", superClassMatch, apiObject);
        log.debug("Interfaces are {}", interfaces);

        Enhancer enh = new Enhancer();
        enh.setSuperclass(superClassMatch);
        enh.setInterfaces(interfaces.toArray(new Class<?>[interfaces.size()]));
        enh.setCallbacks(new Callback[] { invocationHandler, NoOp.INSTANCE });
        enh.setCallbackFilter(new CallbackFilter() {
            // delegate only public methods
            public int accept(Method method) { return Modifier.isPublic(method.getModifiers()) ? 0 : 1; }
        });
        enh.setClassLoader(getTargetApiClassLoader());

        Class<?>[] minConstructorParams = findMinConstructorParams(superClassMatch);
        log.debug("Will attempt to proxy using the superclass constructor with params {}",
            Arrays.asList(minConstructorParams));

        return enh.create(minConstructorParams, new Object[minConstructorParams.length]);
	}

    private Class<?>[] findMinConstructorParams(Class<?> klass) {
        SortedSet<Class<?>[]> constructorParams = new TreeSet<Class<?>[]>(new Comparator<Class<?>[]>() {
            public int compare(Class<?>[] o1, Class<?>[] o2) {
                return o1.length - o2.length;
            }
        });
        for (Constructor<?> constructor : klass.getDeclaredConstructors()) {
            constructorParams.add(constructor.getParameterTypes());
        }
        return constructorParams.size() == 0 ? new Class[0] : constructorParams.first();
    }

	protected Object createProxyForInterfaces(Object apiObject, Class<?>[] interfacesMatch) {
		return java.lang.reflect.Proxy.newProxyInstance(
				getTargetApiClassLoader(),
				interfacesMatch,
				new Delegator(apiObject, this));
	}
	
	
	protected Set<Class<?>> findIntefacesMatch(Class<?> clazz) {
		Set<Class<?>> result = getCache().getInterfaceMap().get(clazz);
		if (result == null) {
			result = new HashSet<Class<?>>();
			for (Class<?> interfac : ClassUtils.getAllInterfaces(clazz)) {
				if (isOfApiPackages(interfac)) {
					result.add(findClassMatch(interfac));
				}
			}
			getCache().getInterfaceMap().put(clazz, result);
		}
		return result;
	}
	
	protected Class<?> findSuperClassMatch(Class<?> clazz) {
		if (getCache().getSuperClassMap().containsKey(clazz)) {
			return getCache().getSuperClassMap().get(clazz);
		} else {
			Class<?> superClassMatch = null;
			while (clazz != null) {
				if (isOfApiPackages(clazz)) {
					superClassMatch = findClassMatch(clazz);
					break;
				}
				clazz = clazz.getSuperclass();
			}
			
			getCache().getSuperClassMap().put(clazz, superClassMatch);
			return superClassMatch;
		}
	}
	
	protected Class<?> findClassMatch(Class<?> clazz) {
		try {
			return getTargetApiClassLoader().loadClass(clazz.getName());
		} catch (ClassNotFoundException ex) {
			throw new ApiBridgeException(String.format(
					"Was not able to find a matching class '%s' in the target class loader",
					clazz.getName()), ex);
		}
	}
	
	
	protected boolean isOfApiPackages(Class<?> clazz) {
		return getApiPackageNames().contains(ClassUtils.getPackageName(clazz));
	}
	
	
	protected ApiBridge(ClassLoader targetApiClassLoader, String... packageNames) {
		this.targetApiClassLoader = targetApiClassLoader;
		this.apiPackageNames = new HashSet<String>(Arrays.asList(packageNames));
		this.apiPackageNamesArray = apiPackageNames.toArray(new String[0]);
	}
	
	private final ClassLoader targetApiClassLoader;
	public ClassLoader getTargetApiClassLoader() {
		return targetApiClassLoader;
	}
	
	private final Set<String> apiPackageNames;
	public Set<String> getApiPackageNames() {
		return apiPackageNames;
	}
	
	private final String[] apiPackageNamesArray;
	public String[] getApiPackageNamesArray() {
		return apiPackageNamesArray;
	}
	
	private final ApiBridgeCache cache = new ApiBridgeCache();
	public ApiBridgeCache getCache() {
		return cache;
	}
	
	
	public static ApiBridge getApiBridge(ClassLoader targetApiClassLoader, String... packageNames) {
		String apiBridgeKey = formApiBridgeKey(targetApiClassLoader, packageNames);
		ApiBridge apiBridge = getApiBridges().get(apiBridgeKey);
		if (apiBridge == null) {
			apiBridge = new ApiBridge(targetApiClassLoader, packageNames);
		}
		return apiBridge;
	}

    public ApiBridge getReverseApiBridge(ClassLoader sourceApiClassLoader) {
        return ApiBridge.getApiBridge(
                ApiBridgeClassLoader.getClassLoader(getTargetApiClassLoader(),
                        sourceApiClassLoader, getApiPackageNamesArray()),
                        getApiPackageNamesArray());
    }
	
	private static Map<String, ApiBridge> apiBridges = new HashMap<String, ApiBridge>();
	private static Map<String, ApiBridge> getApiBridges() {
		return apiBridges;
	}
	
	private static String formApiBridgeKey(ClassLoader targetApiClassLoader, String... packageNames) {
		StringBuffer result = new StringBuffer();
		result.append(targetApiClassLoader.hashCode());
		result.append('-');
		for (String packageName : packageNames) {
			result.append(packageName.hashCode());
			result.append(':');
		}
		return result.toString();
	}
	
}
