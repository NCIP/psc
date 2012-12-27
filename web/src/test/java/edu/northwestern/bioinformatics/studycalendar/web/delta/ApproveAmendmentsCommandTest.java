/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.delta;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.nwu.bioinformatics.commons.DateUtils;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import gov.nih.nci.cabig.ctms.lang.StaticNowFactory;
import org.easymock.IArgumentMatcher;
import static org.easymock.classextension.EasyMock.*;

import java.sql.Timestamp;
import static java.util.Calendar.*;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class ApproveAmendmentsCommandTest extends StudyCalendarTestCase {
    private static final Timestamp NOW = DateTools.createTimestamp(2007, MARCH, 5);

    private ApproveAmendmentsCommand command;
    private StudySite studySite;
    private Amendment a2003, a2004, a2005;
    private AmendmentService amendmentService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);

        Study study = new Study();
        a2005 = createAmendments(
            DateUtils.createDate(2003, MARCH, 5),
            DateUtils.createDate(2004, SEPTEMBER, 9),
            DateUtils.createDate(2005, FEBRUARY, 2)
        );
        a2004 = a2005.getPreviousAmendment();
        a2003 = a2004.getPreviousAmendment();
        study.setAmendment(a2005);

        studySite = createStudySite(study, new Site());
        studySite.approveAmendment(a2003, DateUtils.createDate(2003, APRIL, 1));

        initCommand();
    }

    private void initCommand() {
        StaticNowFactory nowFactory = new StaticNowFactory();
        nowFactory.setNowTimestamp(NOW);
        command = new ApproveAmendmentsCommand(studySite, amendmentService, nowFactory);
    }

    public void testApprovalSubcommandForApprovedAmendment() throws Exception {
        ApproveAmendmentsCommand.Approval actual2003Approval = command.getApprovals().get(0);
        assertNotNull(actual2003Approval);
        assertSame(a2003, actual2003Approval.getAmendment());
        assertTrue(actual2003Approval.isAlreadyApproved());
        assertFalse(actual2003Approval.isJustApproved());
        assertDayOfDate(2003, APRIL, 1, actual2003Approval.getDate());
    }

    public void testSkeletonApprovalSubcommandForUnapprovedAmendment() throws Exception {
        ApproveAmendmentsCommand.Approval actual2004Approval = command.getApprovals().get(1);
        assertNotNull(actual2004Approval);
        assertSame(a2004, actual2004Approval.getAmendment());
        assertFalse(actual2004Approval.isAlreadyApproved());
        assertFalse(actual2004Approval.isJustApproved());
        assertDayOfDate("Date not initialized to now",
            2007, MARCH, 5, actual2004Approval.getDate());
    }

    public void testApply() throws Exception {
        assertEquals("Test setup failure", 1, studySite.getAmendmentApprovals().size());

        Date expectedDate = DateTools.createDate(2005, SEPTEMBER, 2);
        command.getApprovals().get(1).setJustApproved(true);
        command.getApprovals().get(1).setDate(expectedDate);

        amendmentService.approve(eq(studySite), eqApproval(a2004, expectedDate));
        
        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testApplyAutomaticallyApprovesPreviousApprovedAmendments() throws Exception {
        // need a fourth amendment to test all cases
        Amendment a2002 = createAmendments(DateTools.createDate(2002, SEPTEMBER, 22));
        a2003.setPreviousAmendment(a2002);
        studySite.getAmendmentApprovals().clear();
        studySite.approveAmendment(a2002, DateTools.createDate(2002, NOVEMBER, 4));
        initCommand();

        Date expectedDate = DateTools.createDate(2005, DECEMBER, 8);
        command.getApprovals().get(2).setJustApproved(true);
        command.getApprovals().get(2).setDate(expectedDate);
        assertSame("Test setup failure", a2004, command.getApprovals().get(2).getAmendment());

        amendmentService.approve(eq(studySite),
            eqApproval(a2003, expectedDate),
            eqApproval(a2004, expectedDate)
        );

        replayMocks();
        command.apply();
        verifyMocks();
    }

    //////

    private static AmendmentApproval eqApproval(Amendment amendment, Date date) {
        reportMatcher(new AmendmentApprovalMatcher(amendment, date));
        return null;
    }

    private static class AmendmentApprovalMatcher implements IArgumentMatcher {
        private Amendment expectedAmendment;
        private Date expectedDate;

        public AmendmentApprovalMatcher(Amendment amendment, Date date) {
            expectedAmendment = amendment;
            expectedDate = date;
        }

        public boolean matches(Object object) {
            if (object instanceof AmendmentApproval) {
                AmendmentApproval actual = (AmendmentApproval) object;
                return actual.getAmendment().equals(expectedAmendment)
                    && DateTools.daysEqual(expectedDate, actual.getDate());
            } else {
                return false;
            }
        }

        public void appendTo(StringBuffer sb) {
            sb.append("AmendmentApproval with date=").append(expectedDate)
                .append(" and amendment ").append(expectedAmendment);
        }
    }
}
