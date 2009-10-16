package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import junit.framework.TestCase;

/**
 * @author Rhett Sutphin
 */
public class PaOperationTest extends TestCase {
    public void testProtocolServiceTypeIsCorrect() throws Exception {
        assertEquals("STUDY_PROTOCOL", PaOperation.GET_STUDY_PROTOCOL.getServiceType());
    }

    public void testSiteServiceTypeIsCorrect() throws Exception {
        assertEquals("STUDY_SITE", PaOperation.GET_STUDY_SITES_BY_PROTOCOL.getServiceType());
    }
}
