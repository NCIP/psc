package edu.northwestern.bioinformatics.studycalendar.restlets;

/**
 * @author John Dzak
 */
public class ScheduledCalendarResourceTest extends AuthorizedResourceTestCase<ScheduledCalendarResource> {
    private static final String STUDY_IDENTIFIER = "EC golf";
    private static final String STUDY_IDENTIFIER_ENCODED = "EC+golf";
    private static final String ASSIGNMENT_IDENTIFIER = "assignment-grid-0";

    protected void setUp() throws Exception {
        super.setUp();

        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_IDENTIFIER_ENCODED);
        request.getAttributes().put(UriTemplateParameters.ASSIGNMENT_IDENTIFIER.attributeName(), ASSIGNMENT_IDENTIFIER);
    }

    protected ScheduledCalendarResource createResource() {
        return new ScheduledCalendarResource();
    }

    public void testGetXmlForScheduledStudies() {
        assertTrue(true);
        // expect find scheduled calendar through study and assignment
        // expect that two calendars are found and passed to the createDocument method
        // 
    }
}
