/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ConcreteStaticApplicationContextTest extends TestCase {
    public void testContextContainsExplicitBeans() throws Exception {
        Integer a = 42;
        List<Integer> c = Arrays.asList(6, 8);
        ApplicationContext actual = ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().
                put("a", a).
                put("c", c).
                toMap());

        assertSame(a, actual.getBean("a"));
        assertSame(c, actual.getBean("c"));
    }
}
