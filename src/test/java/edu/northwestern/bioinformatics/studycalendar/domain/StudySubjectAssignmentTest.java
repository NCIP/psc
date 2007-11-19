package edu.northwestern.bioinformatics.studycalendar.domain;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectTools;

import java.util.List;
import java.util.Collection;

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

    private void addAdverseEventNotification(int aeId) {
        AdverseEventNotification notification = setId(aeId, new AdverseEventNotification());
        AdverseEvent event = setId(aeId, new AdverseEvent());
        notification.setAdverseEvent(event);
        assignment.addAeNotification(notification);
    }
}
