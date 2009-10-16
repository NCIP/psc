package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class PoOperationTest extends TestCase {
    public void testOrgServiceTypeIsDerivedFromClientClass() throws Exception {
        assertEquals("ORGANIZATION", PoOperation.GET_ORGANIZATION.getServiceType());
    }

    public void testResOrgServiceTypeIsDerivedFromClientClass() throws Exception {
        assertEquals("RESEARCH_ORGANIZATION", PoOperation.GET_RESEARCH_ORGANIZATIONS.getServiceType());
    }
}
