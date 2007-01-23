package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class StudyParticipantAssignmentDaoTest extends ContextDaoTestCase<StudyParticipantAssignmentDao> {
    public void testGetById() throws Exception {
        StudyParticipantAssignment assignment = getDao().getById(-10);

        assertEquals("Wrong id", -10, (int) assignment.getId());
        CoreTestCase.assertDayOfDate("Wrong start date", 2003, Calendar.FEBRUARY, 1,
            assignment.getStartDateEpoch());
        assertEquals("Wrong participant", -20, (int) assignment.getParticipant().getId());
        assertEquals("Wrong study site", -15, (int) assignment.getStudySite().getId());
        assertEquals("Wrong study id", "004-12", assignment.getStudyId());
    }

    public void testGetByBigId() throws Exception {
        StudyParticipantAssignment assignment = getDao().getByBigId("NOT-SMALL");
        assertNotNull(assignment);
        assertEquals("Wrong obj returned", -10, (int) assignment.getId());
    }

    public void testAesSaved() throws Exception {
        {
            StudyParticipantAssignment assignment = getDao().getById(-10);
            assertEquals("Should already be one", 1, assignment.getAeNotifications().size());
            AdverseEventNotification notification = new AdverseEventNotification();
            AdverseEvent event = new AdverseEvent();
            event.setDescription("Big bad");
            event.setDetectionDate(DateUtils.createDate(2006, Calendar.APRIL, 5));
            notification.setAdverseEvent(event);

            assignment.addAeNotification(notification);
        }

        interruptSession();

        StudyParticipantAssignment reloaded = getDao().getById(-10);
        assertEquals("Wrong number of notifications", 2, reloaded.getAeNotifications().size());
        AdverseEventNotification notification = reloaded.getAeNotifications().get(1);
        assertNotNull(notification.getId());
        assertFalse(notification.isDismissed());
        assertEquals("Big bad", notification.getAdverseEvent().getDescription());
        CoreTestCase.assertDayOfDate(2006, Calendar.APRIL, 5, notification.getAdverseEvent().getDetectionDate());
    }
}
