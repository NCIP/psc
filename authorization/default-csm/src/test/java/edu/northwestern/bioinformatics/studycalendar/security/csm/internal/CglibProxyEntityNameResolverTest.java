/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.csm.internal;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.NoOp;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class CglibProxyEntityNameResolverTest {
    private CglibProxyEntityNameResolver resolver;

    @Before
    public void before() throws Exception {
        resolver = new CglibProxyEntityNameResolver();
    }

    @Test
    public void itStripsOffTheCglibSuffixWhenPresent() throws Exception {
        Enhancer enh = new Enhancer();
        enh.setSuperclass(ArrayList.class);
        enh.setCallback(NoOp.INSTANCE);
        Object o = enh.create();

        assertThat(resolver.resolveEntityName(o), is("java.util.ArrayList"));
    }

    @Test
    public void itReturnsNullIfTheObjectIsNotACglibProxy() throws Exception {
        assertThat(resolver.resolveEntityName("some string"), is(nullValue()));
    }
}
