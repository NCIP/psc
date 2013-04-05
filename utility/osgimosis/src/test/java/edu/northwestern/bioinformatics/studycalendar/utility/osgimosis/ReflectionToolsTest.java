/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis;

import junit.framework.TestCase;

import java.io.Serializable;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.lang.reflect.Method;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "RawUseOfParameterizedType" })
public class ReflectionToolsTest extends TestCase {
    public void testGetInterfacesGetsSuperinterfacesOfParameter() throws Exception {
        List<Class> actual = Arrays.asList(ReflectionTools.getAllInterfaces(List.class));
        List<Class<? extends Iterable>> expected = Arrays.asList(
            List.class, Collection.class, Iterable.class
        );
        for (Class<?> expectedInterface : expected) {
            assertTrue("Missing " + expectedInterface, actual.contains(expectedInterface));
        }
        assertEquals("Wrong number of interfaces: " + actual, expected.size(), actual.size());
    }
    
    public void testGetAllInterfacesGetsInterfacesOfSuperclasses() throws Exception {
        List<Class> actual = Arrays.asList(ReflectionTools.getAllInterfaces(HashSet.class));
        List<Class<?>> expected = Arrays.asList(
            Set.class, Collection.class, Iterable.class, Serializable.class, Cloneable.class
        );
        for (Class<?> expectedInterface : expected) {
            assertTrue("Missing " + expectedInterface, actual.contains(expectedInterface));
        }
        assertEquals("Wrong number of interfaces: " + actual, expected.size(), actual.size());
    }

    public void testGetPackageName() throws Exception {
        assertEquals("java.util", ReflectionTools.getPackageName(List.class));
    }

    public void testFindMethodWithNoParameters() throws Exception {
        assertEquals("size", ReflectionTools.findMethod(Map.class, "size").getName());
    }

    public void testFindsCorrectVersionOfOverloadedMethod() throws Exception {
        Method actual = ReflectionTools.findMethod(PrintStream.class, "print", Integer.TYPE);
        assertEquals(1, actual.getParameterTypes().length);
        assertEquals("int", actual.getParameterTypes()[0].getName());

        actual = ReflectionTools.findMethod(PrintStream.class, "print", Character.TYPE);
        assertEquals(1, actual.getParameterTypes().length);
        assertEquals("char", actual.getParameterTypes()[0].getName());
    }

    public void testFindMethodPrefersMethodsFromInterfaces() throws Exception {
        Method actual = ReflectionTools.findMethod(ArrayList.class, "get", Integer.TYPE);
        assertEquals(List.class, actual.getDeclaringClass());
    }
}
