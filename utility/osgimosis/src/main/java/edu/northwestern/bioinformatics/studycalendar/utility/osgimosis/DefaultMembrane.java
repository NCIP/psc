/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A full-featured implementation of {@link Membrane} which uses proxies to allow
 * objects to live in multiple classloader environments.
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class DefaultMembrane implements Membrane {
    private static final String DEPTH_MDC_KEY = "depth";
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Logger locationLog = LoggerFactory.getLogger(getClass().getName() + ".locations");

    private Collection<String> sharedPackages;
    private Map<EncapsulatorCacheKey, Encapsulator> encapsulators;

    private BidirectionalObjectStore cache;
    private ClassLoader nearClassLoader;
    private Map<String, Object[]> proxyConstructorParams;

    public DefaultMembrane() {
        this.encapsulators = new HashMap<EncapsulatorCacheKey, Encapsulator>();
        this.cache = new BidirectionalObjectStore();
        this.proxyConstructorParams = new HashMap<String, Object[]>();
    }

    public DefaultMembrane(ClassLoader nearClassLoader, String... packages) {
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
        return this.traverse(object, newCounterpartClassLoader,
            // Pass along the near-side ClassLoader to use when proxying parameters & return values.
            newCounterpartClassLoader == nearClassLoader ? null : nearClassLoader);
    }

    /**
     * Traverse the membrane with the designated object.  If the object has previously traversed
     * the membrane, the previously created encapsulated version will be returned.  If it has not,
     * a new encapsulated version will be created using the two parameter class loaders.
     *
     * @param object
     * @param newCounterpartClassLoader If necessary, specifies the class loader where the
     *  encapsulated version of <code>object</code> will be created.
     * @param newCounterpartReverseClassLoader If necessary, specifies the class loader
     *  that will be used to encapsulate any parameters/return values of the encapsulated
     *  object.  If null, this will be guessed.
     * @return
     */
    @SuppressWarnings({ "unchecked" })
    public Object traverse(Object object, ClassLoader newCounterpartClassLoader, ClassLoader newCounterpartReverseClassLoader) {
        pushMDC();
        try {
            log.trace("Traversing {} with {}", this, object);
            if (object == null) {
                log.trace(" - Null is null no matter where you're from");
                return null;
            }
            log.trace(" - Identity: {}@{}", object.getClass().getName(),
                Integer.toHexString(System.identityHashCode(object)));
            log.trace(" - From {}", object.getClass().getClassLoader());

            if (newCounterpartClassLoader == null) {
                log.trace(" - Not bridging object into bootstrap classloader");
                return object;
            }

            log.trace(" - Into {}", newCounterpartClassLoader);
            log.trace("   (using {} as the reverse)", newCounterpartReverseClassLoader);
            if (locationLog.isTraceEnabled()) {
                StackTraceElement[] stacktrace;
                try { throw new RuntimeException(); } catch (RuntimeException re) { stacktrace = re.getStackTrace(); }
                locationLog.trace(" - At {}", stacktrace[0]);
                for (int i = 1; i < stacktrace.length; i++) {
                    locationLog.trace("      {}", stacktrace[i]);
                }
            }
            if (cache.get(object) == null) {
                try {
                    Encapsulator encapsulator = getEncapsulator(object, newCounterpartClassLoader, newCounterpartReverseClassLoader);
                    if (encapsulator == null) {
                        log.trace(" - Not encapsulatable; returning original object");
                        return object;
                    } else {
                        log.trace(" - Building new proxy");
                        cache.put(encapsulator.encapsulate(object), object);
                    }
                } catch (MembraneException e) {
                    log.error(
                        String.format("Encapsulating %s (%s@%s) for %s failed", 
                            object, object.getClass().getName(),
                            Integer.toHexString(System.identityHashCode(object)),
                            newCounterpartClassLoader),
                        e);
                    throw e;
                }
            } else {
                log.trace(" - Reusing cached value");
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

    private Encapsulator getEncapsulator(Object toEncapsulate, ClassLoader toEncapsulateFor, ClassLoader toEncapsulateBackTo) {
        EncapsulatorCacheKey key = new EncapsulatorCacheKey(toEncapsulate.getClass().getName(), toEncapsulateFor, toEncapsulateBackTo);
        if (!encapsulators.containsKey(key)) {
            log.trace(" - Creating new Encapsulator");
            encapsulators.put(key, new DefaultEncapsulatorCreator(
                this, toEncapsulate.getClass(), toEncapsulateFor, toEncapsulateBackTo, proxyConstructorParams).create());
        } else {
            log.trace(" - Using cached Encapsulator");
        }
        return encapsulators.get(key);
    }

    ////// CONFIGURATION

    public void setNearClassLoader(ClassLoader nearClassLoader) {
        log.debug("nearClassLoader={}", nearClassLoader);
        this.nearClassLoader = nearClassLoader;
    }

    public void setSharedPackages(Collection<String> sharedPackages) {
        log.trace("sharedPackages={}", sharedPackages);
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

    private static class EncapsulatorCacheKey {
        private String className;
        private ClassLoader clFor, clBackTo;

        private EncapsulatorCacheKey(String className, ClassLoader clFor, ClassLoader clBackTo) {
            this.className = className;
            this.clFor = clFor;
            this.clBackTo = clBackTo;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof EncapsulatorCacheKey)) return false;

            EncapsulatorCacheKey that = (EncapsulatorCacheKey) o;

            return clBackTo == that.clBackTo && clFor == that.clFor && className.equals(that.className);
        }

        @Override
        public int hashCode() {
            int result = className != null ? className.hashCode() : 0;
            result = 31 * result + (clFor != null ? clFor.hashCode() : 0);
            result = 31 * result + (clBackTo != null ? clBackTo.hashCode() : 0);
            return result;
        }
    }
}
