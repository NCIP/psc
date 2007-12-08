package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;

import java.util.List;
import java.util.Collection;
import java.util.Calendar;
import static java.util.Calendar.*;

import gov.nih.nci.cabig.ctms.lang.DateTools;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentTest extends StudyCalendarTestCase {
    private StudySubjectAssignment assignment = new StudySubjectAssignment();
    
    public void testSetCalendarMaintainsBidrectionality() throws Exception {
        ScheduledCalendar calendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(calendar);
        assertSame(assignment, calendar.getAssignment());
    }

    public void testAddAeNotificationMaintainsBidirectionality() throws Exception {
        AdverseEventNotification notification = new AdverseEventNotification();
        assignment.addAeNotification(notification);
        assertSame(assignment, notification.getAssignment());
    }
    
    public void testGetCurrentNotifications() throws Exception {
        addAdverseEventNotification(1);
        addAdverseEventNotification(2);
        addAdverseEventNotification(3);
        assignment.getAeNotifications().get(1).setDismissed(true);
        assertEquals(3, assignment.getAeNotifications().size());

        Collection<Integer> currentAeIds = DomainObjectTools.collectIds(assignment.getCurrentAeNotifications());
        assertEquals(2, currentAeIds.size());
        assertContains(currentAeIds, 1);
        assertContains(currentAeIds, 3);
    }

    public void testGetAvailableUnappliedAmendments() throws Exception {
        Study study = new Study();
        StudySite augusta = createStudySite(study, createNamedInstance("Augusta", Site.class));
        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        Amendment a1 = a2.getPreviousAmendment();
        Amendment a0 = a1.getPreviousAmendment();
        study.setAmendment(a3);

        augusta.approveAmendment(a0, DateTools.createDate(2003, AUGUST, 1));
        augusta.approveAmendment(a1, DateTools.createDate(2003, AUGUST, 2));
        augusta.approveAmendment(a2, DateTools.createDate(2003, AUGUST, 3));
        // a3 not approved

        assignment.setStudySite(augusta);
        assignment.setCurrentAmendment(a0);

        List<Amendment> actual = assignment.getAvailableUnappliedAmendments();
        assertEquals("Wrong number of amendments returned", 2, actual.size());
        assertEquals("Wrong amendment available", a1, actual.get(0));
        assertEquals("Wrong amendment available", a2, actual.get(1));
    }

    private void addAdverseEventNotification(int aeId) {
        AdverseEventNotification notification = setId(aeId, new AdverseEventNotification());
        AdverseEvent event = setId(aeId, new AdverseEvent());
        notification.setAdverseEvent(event);
        assignment.addAeNotification(notification);
    }
}
