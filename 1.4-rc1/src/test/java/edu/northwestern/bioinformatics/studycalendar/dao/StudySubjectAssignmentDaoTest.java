package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.nwu.bioinformatics.commons.testing.CoreTestCase;
import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEventNotification;
import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentDaoTest extends ContextDaoTestCase<StudySubjectAssignmentDao> {
    public void testGetById() throws Exception {
        StudySubjectAssignment assignment = getDao().getById(-10);

        assertEquals("Wrong id", -10, (int) assignment.getId());
        CoreTestCase.assertDayOfDate("Wrong start date", 2003, Calendar.FEBRUARY, 1,
            assignment.getStartDateEpoch());
        CoreTestCase.assertDayOfDate("Wrong end date", 2003, Calendar.SEPTEMBER, 1,
            assignment.getEndDateEpoch());
        assertEquals("Wrong subject", -20, (int) assignment.getSubject().getId());
        assertEquals("Wrong study site", -15, (int) assignment.getStudySite().getId());
        assertEquals("Wrong study id", "004-12", assignment.getStudyId());
        assertEquals("Wrong current amendment", -18, (int) assignment.getCurrentAmendment().getId());
        assertEquals("Wrong number of populations", 1, assignment.getPopulations().size());
        assertEquals("Wrong population", -21, (int) assignment.getPopulations().iterator().next().getId());
    }

    public void testGetByGridId() throws Exception {
        StudySubjectAssignment assignment = getDao().getByGridId("NOT-SMALL");
        assertNotNull(assignment);
        assertEquals("Wrong obj returned", -10, (int) assignment.getId());
    }

    public void testAesSaved() throws Exception {
        {
            StudySubjectAssignment assignment = getDao().getById(-10);
            assertEquals("Should already be one", 1, assignment.getAeNotifications().size());
            AdverseEventNotification notification = new AdverseEventNotification();
            AdverseEvent event = new AdverseEvent();
            event.setDescription("Big bad");
            event.setDetectionDate(DateUtils.createDate(2006, Calendar.APRIL, 5));
            notification.setAdverseEvent(event);

            assignment.addAeNotification(notification);
        }

        interruptSession();

        StudySubjectAssignment reloaded = getDao().getById(-10);
        assertEquals("Wrong number of notifications", 2, reloaded.getAeNotifications().size());
        AdverseEventNotification notification = reloaded.getAeNotifications().get(1);
        assertNotNull(notification.getId());
        assertFalse(notification.isDismissed());
        assertEquals("Big bad", notification.getAdverseEvent().getDescription());
        CoreTestCase.assertDayOfDate(2006, Calendar.APRIL, 5, notification.getAdverseEvent().getDetectionDate());
    }
}
