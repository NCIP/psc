/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class UriTemplateTest extends TestCase {
    private UriTemplate.Resolver resolver;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        resolver = new UriTemplate.MapResolver(
            new MapBuilder<String, String>().
                put("alpha", "5").
                put("gamma", "radiation").
                put("sigma sigma", "one-third").
                toMap()
        );
    }

    public void testSingleVariableTemplateReplaced() throws Exception {
        assertEquals("5", new UriTemplate("{alpha}").format(resolver));
    }

    public void testMultipleVariablesReplaced() throws Exception {
        assertEquals("https://www.google.com/?q=5&source=radiation&a=b",
            new UriTemplate("https://www.google.com/?q={alpha}&source={gamma}&a=b").
                format(resolver));
    }

    public void testUnresolvedVariablesReplacedWithNothing() throws Exception {
        assertEquals("A=5 B= G=radiation",
            new UriTemplate("A={alpha} B={beta} G={gamma}").format(resolver));
    }

    public void testVariablesWithSpacesReplaced() throws Exception {
        assertEquals("one-third", new UriTemplate("{sigma sigma}").format(resolver));
    }
}
