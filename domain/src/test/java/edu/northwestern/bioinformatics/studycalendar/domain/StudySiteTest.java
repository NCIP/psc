/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import gov.nih.nci.cabig.ctms.lang.DateTools;
import junit.framework.TestCase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import static edu.northwestern.bioinformatics.studycalendar.domain.StudySite.findStudySite;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;

/**
 * @author Rhett Sutphin
 */
public class StudySiteTest extends TestCase {
    private StudySite studySite;
    private Amendment aOrig, a2003, a2004, a2005;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a2005 = createAmendments(
            DateTools.createDate(2003, Calendar.DECEMBER, 1),
            DateTools.createDate(2004, Calendar.DECEMBER, 1),
            DateTools.createDate(2005, Calendar.DECEMBER, 1)
        );
        a2004 = a2005.getPreviousAmendment();
        a2003 = a2004.getPreviousAmendment();

        Study study = Fixtures.createReleasedTemplate();
        study.setName("Picnic");
        aOrig = study.getAmendment();
        a2003.setPreviousAmendment(study.getAmendment());
        study.setAmendment(a2005);
        Site site = Fixtures.createNamedInstance("Galesburg", Site.class);
        studySite = Fixtures.createStudySite(study, site);

        FormatTools.setLocal(new FormatTools("MM/dd/yyyy"));
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FormatTools.clearLocalInstance();
    }

    public void testUsedWhenUsed() throws Exception {
        StudySubjectAssignment assignment = new StudySubjectAssignment();
        studySite.getStudySubjectAssignments().add(assignment);
        assertTrue(studySite.isUsed());
    }

    public void testUsedWhenNotUsed() throws Exception {
        assertFalse(studySite.isUsed());
    }
    
    public void testApproveAmendment() throws Exception {
        assertEquals("Test setup failure", 0, studySite.getAmendmentApprovals().size());

        studySite.approveAmendment(a2003, DateTools.createDate(2003, Calendar.DECEMBER, 4));
        assertEquals("Approval not added", 1, studySite.getAmendmentApprovals().size());
        AmendmentApproval actual = studySite.getAmendmentApprovals().get(0);
        assertEquals("Wrong amendment approved", a2003, actual.getAmendment());
        assertDayOfDate("Wrong approval date", 2003, Calendar.DECEMBER, 4, actual.getDate());
        assertEquals("Approval did not retain reverse relationship", studySite, actual.getStudySite());
    }

    public void testAddAmendmentApproval() throws Exception {
        assertEquals("Test setup failure", 0, studySite.getAmendmentApprovals().size());

        AmendmentApproval approval = AmendmentApproval.create(a2003, DateTools.createDate(2003, Calendar.DECEMBER, 4));
        studySite.addAmendmentApproval(approval);
        assertEquals("Approval not added", 1, studySite.getAmendmentApprovals().size());
        AmendmentApproval actual = studySite.getAmendmentApprovals().get(0);
        assertSame(approval, actual);
        assertEquals("Wrong amendment approved", a2003, actual.getAmendment());
        assertDayOfDate("Wrong approval date", 2003, Calendar.DECEMBER, 4, actual.getDate());
        assertEquals("Approval did not retain reverse relationship", studySite, actual.getStudySite());
    }

    public void testApproveAmendmentWhenNotInStudy() throws Exception {
        Amendment random = createAmendments(DateTools.createDate(2003, Calendar.MARCH, 1));
        try {
            studySite.approveAmendment(random, DateTools.createDate(2003, Calendar.APRIL, 1));
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("The designated amendment (03/01/2003) is not part of this study", scse.getMessage());
        }
    }

    public void testAddAmendmentApprovalWhenNotInStudy() throws Exception {
        Amendment random = createAmendments(DateTools.createDate(2003, Calendar.MARCH, 1));
        try {
            studySite.addAmendmentApproval(AmendmentApproval.create(random, DateTools.createDate(2003, Calendar.APRIL, 1)));
            fail("Exception not thrown");
        } catch (StudyCalendarSystemException scse) {
            assertEquals("The designated amendment (03/01/2003) is not part of this study", scse.getMessage());
        }
    }

    public void testGetCurrentApprovedAmendment() throws Exception {
        assertNull(studySite.getCurrentApprovedAmendment());
        studySite.approveAmendment(a2003, DateTools.createDate(2003, Calendar.DECEMBER, 6));
        assertSame(a2003, studySite.getCurrentApprovedAmendment());
        studySite.approveAmendment(a2005, DateTools.createDate(2005, Calendar.DECEMBER, 9));
        assertSame(a2005, studySite.getCurrentApprovedAmendment());
        studySite.approveAmendment(a2004, DateTools.createDate(2004, Calendar.DECEMBER, 2));
        assertSame(a2005, studySite.getCurrentApprovedAmendment());
    }

    public void testGetAmendmentApproval() throws Exception {
        studySite.approveAmendment(a2003, DateTools.createDate(2003, Calendar.DECEMBER, 6));
        studySite.approveAmendment(a2004, DateTools.createDate(2004, Calendar.DECEMBER, 2));
        assertNotNull(studySite.getAmendmentApproval(a2003));
        assertNotNull(studySite.getAmendmentApproval(a2004));
        assertNull(studySite.getAmendmentApproval(a2005));

        assertSame(a2003, studySite.getAmendmentApproval(a2003).getAmendment());
        assertDayOfDate(2004, Calendar.DECEMBER, 2, studySite.getAmendmentApproval(a2004).getDate());
    }

    public void testGetUnapprovedAmendments() throws Exception {
        studySite.approveAmendment(aOrig, DateTools.createDate(2003, Calendar.JANUARY, 3));
        studySite.approveAmendment(a2004, DateTools.createDate(2004, Calendar.DECEMBER, 4));
        List<Amendment> actualUnapproved = studySite.getUnapprovedAmendments();
        assertEquals("Wrong amendments: " + actualUnapproved, 2, actualUnapproved.size());
        assertContains(actualUnapproved, a2003);
        assertContains(actualUnapproved, a2005);
    }

    public void testFindStudySite() throws Exception {
        Study study0 = setId(1, createNamedInstance("Study A", Study.class));
        Study study1 = setId(2, createNamedInstance("Study B", Study.class));

        Site site0 = setId(1, createNamedInstance("Site A", Site.class));
        Site site1 = setId(2, createNamedInstance("Site B", Site.class));

        StudySite studySite0 = Fixtures.createStudySite(study0, site0);
        StudySite studySite1 = Fixtures.createStudySite(study0, site1);
        StudySite studySite2 = Fixtures.createStudySite(study1, site0);
        StudySite studySite3 = Fixtures.createStudySite(study1, site1);

        assertEquals("Wrong Study Site", studySite0, findStudySite(study0, site0));
        assertEquals("Wrong Study Site", studySite1, findStudySite(study0, site1));
        assertEquals("Wrong Study Site", studySite2, findStudySite(study1, site0));
        assertEquals("Wrong Study Site", studySite3, findStudySite(study1, site1));
    }

    public void testGetName() throws Exception {
        assertEquals("Picnic: Galesburg", studySite.getName());
    }

    public void testGetNameWithNullSite() throws Exception {
        studySite.setSite(null);
        assertEquals("Picnic: <none>", studySite.getName());
    }

    public void testGetNameWithNullStudy() throws Exception {
        studySite.setStudy(null);
        assertEquals("<none>: Galesburg", studySite.getName());
    }

    public void testGetOnStudyAssignments() throws Exception {
        createAssignment(studySite, createSubject("A", "A"));
        createAssignment(studySite, createSubject("B", "B"));
        createAssignment(studySite, createSubject("C", "C"));

        studySite.getStudySubjectAssignments().get(1).setEndDate(new Date());

        List<StudySubjectAssignment> actual = studySite.getOnStudyAssignments();
        assertEquals("Wrong number of on-study assignments", 2, actual.size());
        assertEquals("Wrong assignment", "A", actual.get(0).getSubject().getFirstName());
        assertEquals("Wrong assignment", "C", actual.get(1).getSubject().getFirstName());
    }

    public void testGetOffStudyAssignments() throws Exception {
        createAssignment(studySite, createSubject("A", "A"));
        createAssignment(studySite, createSubject("B", "B"));
        createAssignment(studySite, createSubject("C", "C"));

        studySite.getStudySubjectAssignments().get(1).setEndDate(new Date());

        List<StudySubjectAssignment> actual = studySite.getOffStudyAssignments();
        assertEquals("Wrong number of off-study assignments", 1, actual.size());
        assertEquals("Wrong user", "B", actual.get(0).getSubject().getFirstName());
    }
}
