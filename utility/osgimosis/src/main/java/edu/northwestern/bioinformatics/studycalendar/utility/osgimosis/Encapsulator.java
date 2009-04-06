package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
class Encapsulator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Membrane membrane;

    private ClassLoader nearClassLoader;
    private Class<?> nearSuperclass;
    private Class<?>[] nearInterfaces;

    // Map from near (proxy) methods to far (concrete) methods
    private Map<Method, Method> farMethodCache;

    public Encapsulator(
        Membrane membrane, ClassLoader nearClassLoader, List<Class> nearInterfaces
    ) {
        this(membrane, nearClassLoader, null, nearInterfaces);
    }

    public Encapsulator(
        Membrane membrane, ClassLoader nearClassLoader, Class nearSuperclass, List<Class> nearInterfaces
    ) {
        if (nearInterfaces.size() == 0 && nearSuperclass == null) {
            throw new IllegalArgumentException(
                "Cannot encapsulate without either a superclass or at least one interface");
        }
        this.membrane = membrane;
        this.nearClassLoader = nearClassLoader;
        this.nearInterfaces = nearInterfaces.toArray(new Class[nearInterfaces.size()]);
        this.nearSuperclass = nearSuperclass;

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

    public Object proxy(Object far) {
        log.trace("Proxying {} in {}", far, nearClassLoader);
        log.trace(" - Identity: {}@{}", far.getClass().getName(), System.identityHashCode(far));
        EncapsulationInterceptor interceptor = new EncapsulationInterceptor(far, this);
        if (nearSuperclass == null) {
            return proxyWithJdk(interceptor);
        } else {
            return proxyWithCglib(interceptor);
        }
    }

    private Object proxyWithJdk(InvocationHandler handler) {
        log.trace(" - In a JDK proxy");
        log.trace(" - With interfaces {}", Arrays.asList(nearInterfaces));
        return Proxy.newProxyInstance(nearClassLoader, nearInterfaces, handler);
    }

    private Object proxyWithCglib(MethodInterceptor interceptor) {
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
        enh.setClassLoader(nearClassLoader);

        return enh.create();
    }

    public Membrane getMembrane() {
        return membrane;
    }
}
