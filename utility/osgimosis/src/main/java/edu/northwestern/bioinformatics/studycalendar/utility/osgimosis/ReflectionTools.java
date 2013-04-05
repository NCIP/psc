/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ReflectionTools {

    public static Class[] getAllInterfaces(Class<?> clazz) {
        Set<Class> interfaces = new HashSet<Class>();

        while (clazz != null) {
            if (clazz.isInterface()) {
                interfaces.add(clazz);
                interfaces.addAll(getAllSuperinterfaces(clazz));
            } else {
                for (Class interfac : clazz.getInterfaces()) {
                    interfaces.add(interfac);
                    interfaces.addAll(getAllSuperinterfaces(interfac));
                }
            }
            clazz = clazz.getSuperclass();
        }
        return interfaces.toArray(new Class[interfaces.size()]);
    }

    private static Collection<Class> getAllSuperinterfaces(Class interfac) {
        if (!interfac.isInterface()) return Collections.emptySet();
        List<Class> firstLevel = Arrays.asList(interfac.getInterfaces());
        Set<Class> superInterfaces = new HashSet<Class>();
        for (Class superI : firstLevel) {
            superInterfaces.addAll(getAllSuperinterfaces(superI));
        }
        superInterfaces.addAll(firstLevel);
        return superInterfaces;
    }

    public static String getPackageName(Class<?> clazz) {
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf('.');
        return (lastDotIndex != -1 ? className.substring(0, lastDotIndex) : "");
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        String[] requiredParamNames = new String[paramTypes.length];
        for (int i = 0; i < requiredParamNames.length; i++) {
            requiredParamNames[i] = paramTypes[i].getName();
        }

        List<Class> toSearch = new ArrayList<Class>();
        toSearch.addAll(Arrays.asList(getAllInterfaces(clazz)));
        toSearch.add(Object.class);
        toSearch.add(clazz);
        for (Class interfac : toSearch) {
            for (Method method : interfac.getMethods()) {
                if (methodMatches(methodName, method, requiredParamNames)) {
                    return method;
                }
            }
        }

        return null;
    }

    private static boolean methodMatches(
        String desiredMethodName, Method actualMethod, String... desiredParamTypeNames
    ) {
        if (actualMethod.getName().equals(desiredMethodName) && actualMethod.getParameterTypes().length == desiredParamTypeNames.length) {
            Class<?>[] actualMethodParamTypes = actualMethod.getParameterTypes();
            for (int i = 0; i < actualMethodParamTypes.length; i++) {
                if (!actualMethodParamTypes[i].getName().equals(desiredParamTypeNames[i])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

}