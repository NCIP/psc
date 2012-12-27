/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ProxyEncapsulator implements ArrayCapableEncapsulator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Membrane membrane;

    private ClassLoader nearClassLoader;
    private Class<?> nearSuperclass;
    private Class<?>[] nearInterfaces;
    private Object[] constructorParams;
    private ClassLoader farClassLoader;

    // Map from near (proxy) methods to far (concrete) methods
    private Map<Method, Method> farMethodCache;

    public ProxyEncapsulator(
        Membrane membrane, ClassLoader nearClassLoader, List<Class> nearInterfaces
    ) {
        this(membrane, nearClassLoader, null, null, nearInterfaces, null);
    }

    public ProxyEncapsulator(
        Membrane membrane, ClassLoader nearClassLoader,
        Class nearSuperclass, Object[] constructorParams, List<Class> nearInterfaces,
        ClassLoader farClassLoader
    ) {
        if (nearInterfaces.size() == 0 && nearSuperclass == null) {
            throw new IllegalArgumentException(
                "Cannot encapsulate without either a superclass or at least one interface");
        }
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
        this.nearInterfaces = nearInterfaces.toArray(new Class[nearInterfaces.size()]);
        this.nearSuperclass = nearSuperclass;
        this.constructorParams = constructorParams;
        this.farClassLoader = farClassLoader;

        this.farMethodCache = new HashMap<Method, Method>();
    }

    public Method findFarMethod(Method nearMethod, Class farType) {
        if (farMethodCache.get(nearMethod) == null) {
            Method farMethod = ReflectionTools.findMethod(
                farType, nearMethod.getName(), nearMethod.getParameterTypes());
            farMethodCache.put(nearMethod, farMethod);
        }
        return farMethodCache.get(nearMethod);
    }

    public Object encapsulate(Object far) {
        try {
            log.trace("Proxying {} in {}", far, nearClassLoader);
            log.trace(" - Identity: {}@{}", far.getClass().getName(), Integer.toHexString(System.identityHashCode(far)));
            EncapsulationInterceptor interceptor = new EncapsulationInterceptor(far, this);
            if (nearSuperclass == null) {
                return proxyWithJdk(interceptor);
            } else {
                return proxyWithCglib(interceptor);
            }
        } catch (RuntimeException t) {
            logProxyError(far, t);
            throw t;
        } catch (Error t) {
            logProxyError(far, t);
            throw t;
        }
    }

    private void logProxyError(Object far, Throwable error) {
        log.error(String.format("There was a problem proxying %s (%s@%s) in classloader %s",
            far, far.getClass().getName(),  Integer.toHexString(System.identityHashCode(far)), nearClassLoader), 
            error);
    }

    public Class<?> componentType() {
        if (nearSuperclass == null) {
            return nearInterfaces[0];
        } else {
            return nearSuperclass;
        }
    }

    protected Object proxyWithJdk(InvocationHandler handler) {
        log.trace(" - In a JDK proxy");
        log.trace(" - With interfaces {}", Arrays.asList(nearInterfaces));
        return Proxy.newProxyInstance(nearClassLoader, nearInterfaces, handler);
    }

    protected Object proxyWithCglib(MethodInterceptor interceptor) {
        Enhancer enhancer = buildCglibEnhancer(interceptor);
        if (constructorParams == null) {
            return enhancer.create();
        } else {
            return enhancer.create(matchingConstructorParamTypes(), constructorParams);
        }
    }

    private Class[] matchingConstructorParamTypes() {
        for (Constructor constructor : nearSuperclass.getConstructors()) {
            Class[] types = constructor.getParameterTypes();
            if (types.length == constructorParams.length) {
                boolean mismatch = false;
                for (int i = 0; i < types.length && !mismatch; i++) {
                    if (constructorParams[i] != null && !types[i].isAssignableFrom(constructorParams[i].getClass())) {
                        mismatch = true;
                    }
                }
                if (!mismatch) return types;
            }
        }
        throw new MembraneException("No constructor in %s which can accept %s",
            nearSuperclass.getName(), Arrays.asList(constructorParams));
    }

    protected Enhancer buildCglibEnhancer(MethodInterceptor interceptor) {
        log.trace(" - As a CGLIB proxy");
        log.trace(" - With superclass {}", nearSuperclass);
        log.trace(" - With interfaces {}", Arrays.asList(nearInterfaces));
        Enhancer enh = new Enhancer();

        enh.setSuperclass(nearSuperclass);
        enh.setInterfaces(nearInterfaces);
        enh.setCallbacks(new Callback[] { interceptor, NoOp.INSTANCE });
        enh.setCallbackFilter(new CallbackFilter() {
            // delegate only public methods
            public int accept(Method method) { return Modifier.isPublic(method.getModifiers()) ? 0 : 1; }
        });
        enh.setUseFactory(false);
        enh.setClassLoader(new SingleUseNearClassLoader());
        return enh;
    }

    public Membrane getMembrane() {
        return membrane;
    }

    public ClassLoader getFarClassLoader() {
        return farClassLoader;
    }

    /**
     * A pure-delegating CL which will have no references outside of the proxy class itself.
     * It exists solely to ensure that every reference to the CGLIB-generated proxy class is
     * GC-able once the proxy object goes out of scope.
     */
    class SingleUseNearClassLoader extends ClassLoader {
        public SingleUseNearClassLoader() {
            super(ProxyEncapsulator.this.nearClassLoader);
        }
    }
}
