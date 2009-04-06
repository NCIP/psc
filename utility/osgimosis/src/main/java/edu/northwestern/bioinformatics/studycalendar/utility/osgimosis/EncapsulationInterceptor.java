package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
class EncapsulationInterceptor implements MethodInterceptor, InvocationHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Object far;
    private ProxyEncapsulator encapsulator;

    public EncapsulationInterceptor(Object far, ProxyEncapsulator encapsulator) {
        this.far = far;
        this.encapsulator = encapsulator;
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        return invoke(proxy, method, args);
    }

    public Object invoke(Object proxy, Method nearMethod, Object[] nearArgs) throws Throwable {
        Method farMethod = getEncapsulator().findFarMethod(nearMethod, far.getClass());
        if (farMethod != null) {
            log.trace("Bridging method {} with args {} in {}",
                new Object[] { nearMethod, nearArgs == null ? "<none>" : Arrays.asList(nearArgs), proxy.getClass() });
            log.trace(" - Far method is {} from {}", farMethod, farMethod.getDeclaringClass());
            try {
                Object farResult = farMethod.invoke(
                    far, encapsulateArgs(nearArgs, farMethod.getParameterTypes()));
                log.trace(" - invocation complete");
                if (farResult == far) {
                    // Prevent infinite recursion when a proxied class's
                    // constructor invokes a method that returns itself.
                    return proxy;
                } else {
                    return getMembrane().farToNear(farResult);
                }
            } catch (InvocationTargetException ite) {
                throw (Throwable) getMembrane().farToNear(ite.getTargetException());
            }
        } else {
            throw new MembraneException(
                "Method '%s' was not found in the delegate object", nearMethod.getName());
        }
    }

    private ProxyEncapsulator getEncapsulator() {
        return encapsulator;
    }

    private Membrane getMembrane() {
        return getEncapsulator().getMembrane();
    }

    private Object[] encapsulateArgs(Object[] nearArgs, Class<?>[] farTypes) {
        if (nearArgs == null) {
            return new Object[0];
        } else {
            Object[] farArgs = new Object[nearArgs.length];
            for (int i = 0; i < nearArgs.length; i++) {
                farArgs[i] = getMembrane().traverse(nearArgs[i], selectFarClassLoader(farTypes[i]));
            }
            return farArgs;
        }
    }

    private ClassLoader selectFarClassLoader(Class<?> parameterType) {
        return parameterType.getClassLoader() == null
            ? far.getClass().getClassLoader()
            : parameterType.getClassLoader();
    }
}
