package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySiteDao;
import edu.northwestern.bioinformatics.studycalendar.web.delta.ApproveAmendmentsCommand;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;
import java.sql.Timestamp;

import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import gov.nih.nci.cabig.ctms.lang.DateTools;

/**
 * @author Rhett Sutphin
 */
public class ApproveAmendmentsCommandTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2007, Calendar.MARCH, 5);

    private ApproveAmendmentsCommand command;
    private StudySite studySite;
    private Amendment a2003, a2004, a2005;
    private StudySiteDao studySiteDao;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        studySiteDao = registerDaoMockFor(StudySiteDao.class);

        Study study = new Study();
        a2005 = createAmendments(
            DateUtils.createDate(2003, Calendar.MARCH, 5),
            DateUtils.createDate(2004, Calendar.SEPTEMBER, 9),
            DateUtils.createDate(2005, Calendar.FEBRUARY, 2)
        );
        a2004 = a2005.getPreviousAmendment();
        a2003 = a2004.getPreviousAmendment();
        study.setAmendment(a2005);

        studySite = createStudySite(study, new Site());
        studySite.approveAmendment(a2003, DateUtils.createDate(2003, Calendar.APRIL, 1));

        StaticNowFactory nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(NOW);
        command = new ApproveAmendmentsCommand(studySite, studySiteDao, nowFactory);
    }

    public void testApprovalSubcommandForApprovedAmendment() throws Exception {
        ApproveAmendmentsCommand.Approval actual2003Approval = command.getApprovals().get(0);
        assertNotNull(actual2003Approval);
        assertSame(a2003, actual2003Approval.getAmendment());
        assertTrue(actual2003Approval.isAlreadyApproved());
        assertFalse(actual2003Approval.isJustApproved());
        assertDayOfDate(2003, Calendar.APRIL, 1, actual2003Approval.getDate());
    }

    public void testSkeletonApprovalSubcommandForUnapprovedAmendment() throws Exception {
        ApproveAmendmentsCommand.Approval actual2004Approval = command.getApprovals().get(1);
        assertNotNull(actual2004Approval);
        assertSame(a2004, actual2004Approval.getAmendment());
        assertFalse(actual2004Approval.isAlreadyApproved());
        assertFalse(actual2004Approval.isJustApproved());
        assertDayOfDate("Date not initialized to now",
            2007, Calendar.MARCH, 5, actual2004Approval.getDate());
    }

    public void testApply() throws Exception {
        assertEquals("Test setup failure", 1, studySite.getAmendmentApprovals().size());

        command.getApprovals().get(1).setJustApproved(true);
        command.getApprovals().get(1).setDate(DateTools.createDate(2005, Calendar.SEPTEMBER, 2));
        studySiteDao.save(studySite);
        
        replayMocks();
        command.apply();
        verifyMocks();

        assertEquals("New approval not registered", 2, studySite.getAmendmentApprovals().size());
        AmendmentApproval oldA = studySite.getAmendmentApprovals().get(0);
        assertEquals("Old approval modified", a2003, oldA.getAmendment());
        assertDayOfDate("Old approval modified", 2003, Calendar.APRIL, 1, oldA.getDate());
        AmendmentApproval newA = studySite.getAmendmentApprovals().get(1);
        assertEquals("New approval incorrect", a2004, newA.getAmendment());
        assertDayOfDate("New approval incorrect", 2005, Calendar.SEPTEMBER, 2, newA.getDate());
        assertSame("New approval incorrect", studySite, newA.getStudySite());
    }
}
