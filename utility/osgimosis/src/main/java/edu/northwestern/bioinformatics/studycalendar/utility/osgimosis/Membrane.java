package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * A membrane represents the boundary between the sets of classes loaded by
 * two separate classloaders.  It has a "near" side and a "far" side and manages
 * taking instances across the boundary in both directions.
 *
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class Membrane {
    private static final String DEPTH_MDC_KEY = "depth";
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Collection<String> sharedPackages;
    private Map<Class, Encapsulator> encapsulators;

    private Cache cache;
    private ClassLoader nearClassLoader;
    private Map<String, Object[]> proxyConstructorParams;

    public Membrane() {
        this.encapsulators = new IdentityHashMap<Class, Encapsulator>();
        this.cache = new Cache();
        this.proxyConstructorParams = new HashMap<String, Object[]>();
    }

    public Membrane(ClassLoader nearClassLoader, String... packages) {
        this();
        this.nearClassLoader = nearClassLoader;
        this.sharedPackages = Arrays.asList(packages);
    }

    @SuppressWarnings({ "unchecked" })
    public Object farToNear(Object farObject) {
        if (nearClassLoader == null) {
            throw new IllegalStateException("nearClassLoader must be set before calling farToNear");
        }
        return traverse(farObject, nearClassLoader);
    }

    @SuppressWarnings({ "unchecked" })
    public Object traverse(Object object, ClassLoader newCounterpartClassLoader) {
        pushMDC();
        try {
            log.debug("Traversing {} with {}", this, object);
            if (object == null) {
                log.trace(" - Null is null no matter where you're from");
                return null;
            }
            log.trace(" - Identity: {}@{}", object.getClass().getName(),
                Integer.toHexString(System.identityHashCode(object)));

            if (newCounterpartClassLoader == null) {
                log.debug(" - Not bridging object into bootstrap classloader");
                return object;
            }

            log.trace(" - Into {}", newCounterpartClassLoader);
            if (cache.get(object) == null) {
                Encapsulator encapsulator = getEncapsulator(object, newCounterpartClassLoader);
                if (encapsulator == null) {
                    log.debug(" - Not encapsulatable; returning original object");
                    return object;
                } else {
                    log.debug(" - Building new proxy");
                    cache.put(encapsulator.encapsulate(object), object);
                }
            } else {
                log.debug(" - Reusing cached value");
            }
            Object result = cache.get(object);
            log.trace(" - Complete with {}@{}", result.getClass().getName(),
                Integer.toHexString(System.identityHashCode(result)));
            return result;
        } finally {
            popMDC();
        }
    }

    public static void pushMDC() {
        String depth = MDC.get(DEPTH_MDC_KEY);
        if (depth == null) depth = "0";
        MDC.put(DEPTH_MDC_KEY, Integer.toString(Integer.parseInt(depth) + 1));
    }

    public static void popMDC() {
        String depth = MDC.get(DEPTH_MDC_KEY);
        if (depth == null) return;
        int newDepth = Integer.parseInt(depth) - 1;
        if (newDepth > 0) {
            MDC.put(DEPTH_MDC_KEY, Integer.toString(newDepth));
        } else {
            MDC.remove(DEPTH_MDC_KEY);
        }
    }

    public void registerProxyConstructorParameters(String className, Object[] parameters) {
        proxyConstructorParams.put(className, parameters);
    }

    private Encapsulator getEncapsulator(Object toEncapsulate, ClassLoader toEncapsulateFor) {
        if (!encapsulators.containsKey(toEncapsulate.getClass())) {
            encapsulators.put(toEncapsulate.getClass(), new DefaultEncapsulatorCreator(
                this, toEncapsulate.getClass(), toEncapsulateFor, proxyConstructorParams).create());
        }
        return encapsulators.get(toEncapsulate.getClass());
    }

    ////// CONFIGURATION

    public void setNearClassLoader(ClassLoader nearClassLoader) {
        this.nearClassLoader = nearClassLoader;
    }

    public void setSharedPackages(Collection<String> sharedPackages) {
        this.sharedPackages = sharedPackages;
    }

    public Collection<String> getSharedPackages() {
        return sharedPackages;
    }

    public void setProxyConstructorParameters(Map<String, List<Object>> params) {
        for (String classname : params.keySet()) {
            registerProxyConstructorParameters(classname, params.get(classname).toArray());
        }
    }

    ////// OBJECT METHODS

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append('@').
            append(Integer.toHexString(System.identityHashCode(this))).
            toString();
    }

    private static class Cache {
        private Map<Object, Object> pairs;

        private Cache() {
            pairs = new IdentityHashMap<Object, Object>();
        }

        public Object get(Object oneValue) {
            return pairs.get(oneValue);
        }

        public synchronized void put(Object one, Object two) {
            pairs.put(one, two);
            pairs.put(two, one);
        }
    }
}
