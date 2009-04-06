package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
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
    private final Logger log = LoggerFactory.getLogger(getClass());

    private List<String> sharedPackages;
    private Map<Class, Encapsulator> encapsulators;

    private Cache cache;
    private ClassLoader nearClassLoader;

    protected Membrane(ClassLoader nearClassLoader, String... packages) {
        this.nearClassLoader = nearClassLoader;
        this.sharedPackages = Arrays.asList(packages);
        this.encapsulators = new IdentityHashMap<Class, Encapsulator>();
        this.cache = new Cache();
    }

    public static Membrane get(ClassLoader nearClassLoader, String... packages) {
        return new Membrane(nearClassLoader, packages);
    }

    @SuppressWarnings({ "unchecked" })
    public Object farToNear(Object farObject) {
        return traverse(farObject, nearClassLoader);
    }

    @SuppressWarnings({ "unchecked" })
    public Object traverse(Object object, ClassLoader newCounterpartClassLoader) {
        log.debug("Traversing {} with {}", this, object);
        if (object == null) {
            log.trace(" - Null is null no matter where you're from");
            return null;
        } else if (object.getClass().getClassLoader() == null) {
            log.debug(" - Not bridging object from bootstrap classloader");
            return object;
        } else if (newCounterpartClassLoader == null) {
            log.debug(" - Not bridging object into bootstrap classloader");
            return object;
        }

        log.trace(" - Identity: {}@{}", object.getClass().getName(),
            Integer.toHexString(System.identityHashCode(object)));
        log.trace(" - Into {}", newCounterpartClassLoader);
        if (cache.get(object) == null) {
            Encapsulator encapsulator = getEncapsulator(object, newCounterpartClassLoader);
            if (encapsulator == null) {
                log.debug(" - Not encapsulatable; returning original object");
                return object;
            } else {
                log.debug(" - Building new proxy");
                cache.put(encapsulator.proxy(object), object);
            }
        } else {
            log.debug(" - Reusing cached value");
        }
        Object result = cache.get(object);
        log.trace(" - Complete with {}@{}", result.getClass().getName(),
            Integer.toHexString(System.identityHashCode(result)));
        return result;
    }

    private Encapsulator getEncapsulator(Object toEncapsulate, ClassLoader toEncapsulateFor) {
        if (!encapsulators.containsKey(toEncapsulate.getClass())) {
            encapsulators.put(toEncapsulate.getClass(),
                new EncapsulatorCreator(this, toEncapsulate.getClass(), toEncapsulateFor).create());
        }
        return encapsulators.get(toEncapsulate.getClass());
    }

    public Collection<String> getSharedPackages() {
        return sharedPackages;
    }

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
