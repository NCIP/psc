/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.schedule;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setGridId;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.Summary;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gov.nih.nci.cabig.ctms.lang.DateTools;

/**
 * @author Saurabh Agrawal
 */
@SuppressWarnings("unchecked")
public class ICalToolsTest extends StudyCalendarTestCase {
    List<ScheduledActivity> scheduledActivities =  new ArrayList<ScheduledActivity>();
    Date date = DateTools.createDate(2006, java.util.Calendar.SEPTEMBER, 20);
    String baseUrl;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ScheduledCalendar scheduledCalendar = new ScheduledCalendar();
        baseUrl = "http://testurl.com";
        Subject subject = Fixtures.createSubject("Perry", "Duglas");
        Study study = Fixtures.createSingleEpochStudy("Study", "Treatment");
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        StudySegment studySegment = epoch.getStudySegments().get(0);
        studySegment.setName("Segment1");
        Site site = Fixtures.createSite("NU");
        StudySubjectAssignment studySubjectAssignment = Fixtures.createAssignment(study,site,subject);
        ScheduledStudySegment scheduledStudySegment =  Fixtures.createScheduledStudySegment(studySegment, DateTools.createDate(2008, java.util.Calendar.MARCH, 1));
        scheduledActivities.add(setGridId("GridId1",Fixtures.createScheduledActivityWithStudy("Activity1", 2006, java.util.Calendar.SEPTEMBER, 20)));
        scheduledActivities.add(setGridId("GridId2",Fixtures.createScheduledActivityWithStudy("Activity2", 2006, java.util.Calendar.SEPTEMBER, 20)));
        ScheduledActivity sa3 = setGridId("GridId3",Fixtures.createScheduledActivityWithStudy("Activity3", 2006, java.util.Calendar.SEPTEMBER, 20));
        sa3.setDetails("Activity Detail");
        scheduledActivities.add(sa3);
        scheduledActivities.add(setGridId("GridId4",Fixtures.createConditionalEventWithStudy("Activity4", 2006, java.util.Calendar.SEPTEMBER, 20)));
        for (ScheduledActivity sa: scheduledActivities) {
            sa.setScheduledStudySegment(scheduledStudySegment);
        }
        scheduledCalendar.addStudySegment(scheduledStudySegment);
        studySubjectAssignment.setScheduledCalendar(scheduledCalendar);
    }

    public void testGenerateCalendarForActivities() throws Exception {
        Calendar calendar = ICalTools.generateCalendarSkeleton();
        ICalTools.generateICSCalendarForActivities(calendar, date, scheduledActivities, baseUrl, false);
        assertEquals("calendar should have 4 events", 4, calendar.getComponents().size());
        assertEquals("calendar should have only 3 properties..", 3, calendar.getProperties().size());
    }

    public void testCalendarEventWithProperties() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(0);
        assertEquals("vEvent should have  8 properties", 8, vEvent.getProperties().size());
        assertNotNull("vEvent should have DtStamp property", vEvent.getProperty(Property.DTSTAMP));
        assertNotNull("vEvent should have UID property", vEvent.getProperty(Property.UID));
        assertNotNull("vEvent should have DtStart property", vEvent.getProperty(Property.DTSTART));
        assertNotNull("vEvent should have DtEnd property", vEvent.getProperty(Property.DTSTART));
        assertNotNull("vEvent should have SUMMARY property", vEvent.getProperty(Property.SUMMARY));
        assertNotNull("vEvent should have URL property", vEvent.getProperty(Property.URL));
        assertNotNull("vEvent should have TRANSP property", vEvent.getProperty(Property.TRANSP));
        assertNotNull("vEvent should have DESCRIPTION property", vEvent.getProperty(Property.DESCRIPTION));
    }

    public void testCalendarEventWithUrlProperty() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(0);
        Property actualUrl = vEvent.getProperty(Property.URL);
        assertNotNull("vEvent should have URL property", actualUrl);
        assertEquals("Activity URl doesn't match", "http://testurl.com/pages/cal/scheduleActivity?event=GridId1", actualUrl.getValue());
    }

    public void testCalendarEventWithUIDProperty() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(0);
        Property uid = vEvent.getProperty(Property.UID);
        assertNotNull("vEvent should have UID property", uid);
        assertEquals("Activity URl doesn't match", "GridId1", uid.getValue());
    }

    public void testCalendarEventWithSummaryProperty() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(0);
        Summary summary = (Summary) vEvent.getProperty("SUMMARY");
        assertNotNull("Summary is empty" , summary);
        assertEquals("Summary value does not match", "Study/Activity1", summary.getValue());
    }

    public void testCalendarEventSummaryForSubjectCoordinator() throws Exception {
        Calendar calendar = ICalTools.generateCalendarSkeleton();
        ICalTools.generateICSCalendarForActivities(calendar, date, scheduledActivities, baseUrl, true);
        List<VEvent> vEvents = calendar.getComponents();
        VEvent vEvent = vEvents.get(0);
        Summary summary = (Summary) vEvent.getProperty("SUMMARY");
        assertEquals("Summary value does not match", "Perry Duglas/Study/Activity1", summary.getValue());
    }

    public void testCalendarEventWithDescriptionProperty() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(0);
        Description description = vEvent.getDescription();
        assertNotNull("Description is empty", description);
        String descriptionValue = description.getValue();
        assertContains(descriptionValue, "Subject:Perry Duglas\n");
        assertContains(descriptionValue, "\nStudy:Study\n");
        assertContains(descriptionValue, "\nStudy Segment:Treatment\n");
        assertContains(descriptionValue, "\nActivity:Activity1\n");
    }

    public void testEventDescriptionWithDetails() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(2);
        assertContains(vEvent.getDescription().getValue(), "\nDetails:Activity Detail\n");
    }

    public void testEventDescriptionWithConditions() throws Exception {
        VEvent vEvent = getCalendarEventOfInxex(3);
        assertContains(vEvent.getDescription().getValue(), "\nCondition:Details\n");
    }


    // Test Helper Methd
    private VEvent getCalendarEventOfInxex(int index) {
        Calendar calendar = ICalTools.generateCalendarSkeleton();
        ICalTools.generateICSCalendarForActivities(calendar, date, scheduledActivities, baseUrl, false);
        List<VEvent> vEvents = calendar.getComponents();
        return vEvents.get(index);
    }
}