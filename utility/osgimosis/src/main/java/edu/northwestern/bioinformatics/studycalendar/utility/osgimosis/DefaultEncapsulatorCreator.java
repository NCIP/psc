package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class DefaultEncapsulatorCreator {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Membrane membrane;
    private Class farClass;
    private ClassLoader nearClassLoader;

    public DefaultEncapsulatorCreator(Membrane membrane, Class farClass, ClassLoader nearClassLoader) {
        this.membrane = membrane;
        this.farClass = farClass;
        this.nearClassLoader = nearClassLoader;
    }

    public Encapsulator create() {
        if (Collection.class.isAssignableFrom(farClass)) {
            return new CollectionEncapsulator(membrane);
        } else if (farClass.isArray()) {
            log.trace(" - Encapsulating array with components {}", farClass.getComponentType());
            Encapsulator componentEncapsulator = new DefaultEncapsulatorCreator(
                membrane, farClass.getComponentType(), nearClassLoader).create();
            if (componentEncapsulator instanceof ArrayCapableEncapsulator) {
                return new ArrayEncapsulator((ArrayCapableEncapsulator) componentEncapsulator);
            } else {
                throw new MembraneException("Cannot encapsulate array; component type is not array capable");
            }
        } else if (nearClassLoader == null) {
            log.trace(" - Not proxying object from bootstrap classloader");
            return null;
        } else {
            Class base = sharedBaseClass();
            List<Class> interfaces = sharedInterfaces();
            if (base != null || !interfaces.isEmpty()) {
                return new ProxyEncapsulator(membrane, nearClassLoader, base, interfaces);
            }
        }

        return null;
    }

    private Class targetClass(Class sourceClass) {
        try {
            return nearClassLoader.loadClass(sourceClass.getName());
        } catch (ClassNotFoundException ex) {
            throw new MembraneException(ex,
                "Was not able to find a matching class '%s' in the target class loader",
                sourceClass.getName());
        }
    }

    private Class sharedBaseClass() {
        Class base = null;
        Class sourceClass = farClass;
        while (sourceClass != null && base == null) {
            if (isInSharedPackage(sourceClass) && hasDefaultConstructor(sourceClass) && !hasFinalMethods(sourceClass)) {
                base = targetClass(sourceClass);
            }
            sourceClass = sourceClass.getSuperclass();
        }
        log.trace("Base class will be {}", base);
        return base;
    }

    private boolean hasFinalMethods(Class sourceClass) {
        for (Method method : sourceClass.getDeclaredMethods()) {
            if (Modifier.isFinal(method.getModifiers())) return true;
        }
        return false;
    }

    private boolean hasDefaultConstructor(Class<?> clazz) {
        try {
            clazz.getConstructor();
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private List<Class> sharedInterfaces() {
        Set<Class> result = new LinkedHashSet<Class>();
        Class[] allInterfaces = ReflectionTools.getAllInterfaces(farClass);
        log.trace("All interfaces are {}", Arrays.asList(allInterfaces));
        for (Class<?> interfac : allInterfaces) {
            if (isInSharedPackage(interfac) && isAvailableInTargetClassLoader(interfac)) {
                result.add(targetClass(interfac));
            }
        }
        log.trace("Selected interfaces are {}", result);
        return new LinkedList<Class>(result);
    }

    private boolean isInSharedPackage(Class<?> interfac) {
        return membrane.getSharedPackages().contains(ReflectionTools.getPackageName(interfac));
    }

    private boolean isAvailableInTargetClassLoader(Class<?> sourceClass) {
        try {
            nearClassLoader.loadClass(sourceClass.getName());
            return true;
        } catch (ClassNotFoundException e) {
            log.debug("{} is in a shared package, but is not available from {}.  Skipping.", sourceClass, nearClassLoader);
            return false;
        }
    }
}
