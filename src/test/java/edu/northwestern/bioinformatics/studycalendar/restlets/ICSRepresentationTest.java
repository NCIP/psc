package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import junit.framework.TestCase;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;

/**
 * @author Saurabh Agrawal
 * @crated Jan 12, 2009
 */
public class ICSRepresentationTest extends TestCase {

    private Representation representation;

    private StudySubjectAssignment studySubjectAssignment;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Subject subject = Fixtures.createSubject("firstName", "lastName");
        StudySite studySite = new StudySite();
        Study study = new Study();
        study.setAssignedIdentifier("test-study");
        studySite.setStudy(study);
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();

        studySubjectAssignment = Fixtures.createAssignment(studySite, subject);
        studySubjectAssignment.setGridId("grid-0");
        studySubjectAssignment.setScheduledCalendar(scheduledCalendar);

    }

    public void testConstructor() {
        representation = new ICSRepresentation(studySubjectAssignment);
        assertNotNull("representation must not be null", representation);
        assertEquals("Result is not right content type", MediaType.TEXT_CALENDAR, representation.getMediaType());
        assertEquals("file name is not correct", "lastName-firstName-test-study.ics", representation.getDownloadName());
        assertTrue("content must be Downloadable", representation.isDownloadable());

    }
}
