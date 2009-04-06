package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import static edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.Membrane.Side.*;
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
        return traverse(NEAR, farObject, nearClassLoader);
    }

    @SuppressWarnings({ "unchecked" })
    public Object nearToFar(Object nearObject, Class farType) {
        return traverse(FAR, nearObject, farType.getClassLoader());
    }

    private Object traverse(Side targetSide, Object object, ClassLoader targetSideClassLoader) {
        log.debug("Traversing {} with {}", this, object);
        log.trace(" - Identity: {}@{}", object.getClass().getName(),
            Integer.toHexString(System.identityHashCode(object)));
        log.trace(" - Into {}", targetSideClassLoader);
        log.trace(" - {} to {}", targetSide.other(), targetSide);
        if (targetSideClassLoader == null) {
            log.trace(" - Not bridging object from bootstrap classloader");
            return object;
        } else if (cache.get(targetSide, object) == null) {
            Encapsulator encapsulator = getEncapsulator(object, targetSideClassLoader);
            if (encapsulator == null) {
                log.debug(" - Not encapsulatable; returning original object");
                cache.put(targetSide, object, object);
            } else {
                log.debug(" - Building new proxy");
                cache.put(targetSide, encapsulator.proxy(object), object);
            }
        } else {
            log.debug(" - Reusing cached value");
        }
        Object result = cache.get(targetSide, object);
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

    public static enum Side {
        NEAR, FAR;

        public Side other() {
            return this == NEAR ? FAR : NEAR;
        }
    }

    private static class Cache {
        private Map<Object, Object> nearToFar, farToNear;

        private Cache() {
            nearToFar = new IdentityHashMap<Object, Object>();
            farToNear = new IdentityHashMap<Object, Object>();
        }

        public Object get(Side side, Object otherSideValue) {
            return getMapForValueSide(side).get(otherSideValue);
        }

        private Map<Object, Object> getMapForValueSide(Side side) {
            return NEAR == side ? farToNear : nearToFar;
        }

        public synchronized void put(Side first, Object one, Object two) {
            if (NEAR == first) {
                nearToFar.put(one, two);
                farToNear.put(two, one);
            } else {
                nearToFar.put(two, one);
                farToNear.put(one, two);
            }
        }
    }
}
