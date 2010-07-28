package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import static java.util.Calendar.*;

/**
 * @author Rhett Sutphin
 */
public class StudySubjectAssignmentTest extends TestCase {
    private StudySubjectAssignment assignment;
    private Site augusta, portland;
    private Subject joe, jane;
    private Study studyA, studyB;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assignment = new StudySubjectAssignment();
        augusta = createNamedInstance("Augusta", Site.class);
        portland = createNamedInstance("Portland", Site.class);
        joe = createSubject("Joe", "B");
        jane = createSubject("Jane", "D");

        studyA = createSingleEpochStudy("A", "E");
        studyB = createSingleEpochStudy("B", "E");
    }

    public void testSetCalendarMaintainsBidrectionality() throws Exception {
        ScheduledCalendar calendar = new ScheduledCalendar();
        assignment.setScheduledCalendar(calendar);
        assertSame(assignment, calendar.getAssignment());
    }

    public void testAddAeNotificationMaintainsBidirectionality() throws Exception {
        Notification notification = new Notification();
        assignment.addAeNotification(notification);
        assertSame(assignment, notification.getAssignment());
    }

    public void testGetCurrentNotifications() throws Exception {
        addAdverseEventNotification(1);
        addAdverseEventNotification(2);
        addAdverseEventNotification(3);
        assignment.getNotifications().get(1).setDismissed(true);
        assertEquals(3, assignment.getNotifications().size());

        Collection<Integer> currentAeIds = DomainObjectTools.collectIds(assignment.getCurrentAeNotifications());
        assertEquals(2, currentAeIds.size());
        assertContains(currentAeIds, 1);
        assertContains(currentAeIds, 3);
    }

    public void testGetAvailableUnappliedAmendments() throws Exception {
        Study study = new Study();
        StudySite ss = createStudySite(study, this.augusta);
        Amendment a3 = createAmendments("A0", "A1", "A2", "A3");
        Amendment a2 = a3.getPreviousAmendment();
        Amendment a1 = a2.getPreviousAmendment();
        Amendment a0 = a1.getPreviousAmendment();
        study.setAmendment(a3);

        ss.approveAmendment(a0, DateTools.createDate(2003, AUGUST, 1));
        ss.approveAmendment(a1, DateTools.createDate(2003, AUGUST, 2));
        ss.approveAmendment(a2, DateTools.createDate(2003, AUGUST, 3));
        // a3 not approved

        assignment.setStudySite(ss);
        assignment.setCurrentAmendment(a0);

        List<Amendment> actual = assignment.getAvailableUnappliedAmendments();
        assertEquals("Wrong number of amendments returned", 2, actual.size());
        assertEquals("Wrong amendment available", a1, actual.get(0));
        assertEquals("Wrong amendment available", a2, actual.get(1));
    }

    public void testStudyNameForSingleAssignment() throws Exception {
        StudySubjectAssignment normal = createAssignment(studyA, augusta, joe);
        assertEquals("Wrong name", "A", normal.getName());
    }

    public void testStudyNameForMultipleAssignmentsToDifferentStudies() throws Exception {
        StudySubjectAssignment onA = createAssignment(studyA, augusta, joe);
        StudySubjectAssignment onB = createAssignment(studyB, augusta, joe);

        assertEquals("A", onA.getName());
        assertEquals("B", onB.getName());
    }

    public void testStudyNameForMultipleAssignmentsToDifferentSites() throws Exception {
        StudySubjectAssignment atAugusta = createAssignment(studyA, augusta, joe);
        StudySubjectAssignment atPortland = createAssignment(studyA, portland, joe);

        assertEquals("A at Augusta", atAugusta.getName());
        assertEquals("A at Portland", atPortland.getName());
    }

    public void testStudyNameForMultipleAssignmentsToTheSameStudyAtTheSameSite() throws Exception {
        StudySubjectAssignment a1 = createAssignment(studyA, portland, joe); a1.setStartDate(new Date());
        StudySubjectAssignment a2 = createAssignment(studyA, portland, joe);

        assertEquals("A (1)", a1.getName());
        assertEquals("A (2)", a2.getName());
    }

    public void testStudyNameForMultipleAssignmentsToTheSameStudyAtTheSameSitePlusAnAssignmentAtAnotherSite() throws Exception {
        StudySubjectAssignment b1 = createAssignment(studyB, portland, joe); b1.setStartDate(new Date());
        StudySubjectAssignment b2 = createAssignment(studyB, augusta, joe);
        StudySubjectAssignment b3 = createAssignment(studyB, portland, joe);

        assertEquals("B at Portland (1)", b1.getName());
        assertEquals("B at Augusta", b2.getName());
        assertEquals("B at Portland (2)", b3.getName());
    }

    public void testIsOffWhenOff() throws Exception {
        assignment.setEndDate(new Date());
        assertTrue(assignment.isOff());
    }

    public void testIsOffWhenNotOff() throws Exception {
        assignment.setEndDate(null);
        assertFalse(assignment.isOff());
    }

    public void testOrderIsEqualForEqualAssignments() throws Exception {
        StudySubjectAssignment a0 = createAssignment(studyA, portland, joe);
        StudySubjectAssignment a1 = createAssignment(studyA, portland, joe);

        assertEquals(0, a0.compareTo(a1));
        assertEquals(0, a1.compareTo(a0));
    }

    public void testDefaultOrderingIsByStudyFirst() throws Exception {
        StudySubjectAssignment a = createAssignment(studyA, portland, joe);
        StudySubjectAssignment b = createAssignment(studyB, augusta, joe);

        assertNegative(a.compareTo(b));
        assertPositive(b.compareTo(a));
    }

    public void testDefaultOrderingIsBySubjectNext() throws Exception {
        StudySubjectAssignment b = createAssignment(studyA, portland, joe);
        StudySubjectAssignment d = createAssignment(studyA, augusta, jane);

        assertNegative(b.compareTo(d));
        assertPositive(d.compareTo(b));
    }

    public void testDefaultOrderingIsBySiteNext() throws Exception {
        StudySubjectAssignment a = createAssignment(studyA, augusta,  joe);
        StudySubjectAssignment p = createAssignment(studyA, portland, joe);

        assertNegative(a.compareTo(p));
        assertPositive(p.compareTo(a));
    }

    public void testOnOffOrderingIsByOnOrOffFirst() throws Exception {
        StudySubjectAssignment a1 = createAssignment(studyA, portland, joe);
        StudySubjectAssignment a2 = createAssignment(studyA, portland, joe);
        a2.setEndDate(new Date());

        assertNegative(StudySubjectAssignment.byOnOrOff().compare(a1, a2));
        assertPositive(StudySubjectAssignment.byOnOrOff().compare(a2, a1));
    }

    public void testOnOffOrderingZeroWhenEquivalent() throws Exception {
        StudySubjectAssignment a1 = createAssignment(studyA, portland, joe);
        a1.setEndDate(new Date());
        StudySubjectAssignment a2 = createAssignment(studyA, portland, joe);
        a2.setEndDate(new Date());

        assertEquals(0, StudySubjectAssignment.byOnOrOff().compare(a1, a2));
        assertEquals(0, StudySubjectAssignment.byOnOrOff().compare(a2, a1));
    }

    public void testOnOffOrderingIsByDefaultOrderNext() throws Exception {
        StudySubjectAssignment b = createAssignment(studyA, portland, joe);
        StudySubjectAssignment d = createAssignment(studyA, portland, jane);

        assertNegative(StudySubjectAssignment.byOnOrOff().compare(b, d));
        assertPositive(StudySubjectAssignment.byOnOrOff().compare(d, b));
    }

    private void addAdverseEventNotification(int aeId) {
        AdverseEvent event = setId(aeId, new AdverseEvent());
        Notification notification = setId(aeId, new Notification(event));
        assignment.addAeNotification(notification);
    }
}
