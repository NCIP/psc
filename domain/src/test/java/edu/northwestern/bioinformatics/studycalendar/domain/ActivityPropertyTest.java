/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.Differences;


/**
 * @author Jalpa Patel
 */
public class ActivityPropertyTest extends DomainTestCase {
    private ActivityProperty ap1, ap2;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ap1 = Fixtures.createActivityProperty("NS", "N", "V");
        ap2 = Fixtures.createActivityProperty("NS", "N", "V");
    }

    public void testDeepEqualsForDifferentNameSpace() throws Exception {
        ap1.setNamespace("Bat");
        assertDifferences(ap1.deepEquals(ap2), "namespace \"Bat\" does not match \"NS\"");
    }

    public void testDeepEqualsForDifferentValue() throws Exception {
        ap2.setValue("11");
        assertDifferences(ap1.deepEquals(ap2), "value \"V\" does not match \"11\"");
    }

    public void testDeepEqualsForDifferentName() throws Exception {
        ap2.setName("G");
        assertDifferences(ap1.deepEquals(ap2), "name \"N\" does not match \"G\"");
    }

    public void testDeepEqualsForSameActivityProperty() throws Exception {
        Differences differences = ap1.deepEquals(ap2);
        assertFalse("Activity Properties are different", differences.hasDifferences());
    }

    public void testNaturalKeyCombinesAllElements() throws Exception {
        assertEquals("NS:N:V", ap1.getNaturalKey());
    }
}
