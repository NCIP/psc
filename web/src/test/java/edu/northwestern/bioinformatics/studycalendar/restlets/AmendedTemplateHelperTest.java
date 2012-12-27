/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.DateUtils;

import java.util.Calendar;
import java.util.Collection;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.*;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateHelperTest extends RestletTestCase {
    private static final String STUDY_NAME = "Mutant Study";
    private static final String STUDY_NAME_ENCODED = "Mutant%20Study";

    private static final String AMENDMENT_KEY_ENCODED = "2007-10-19~Amendment%20B";

    private Study study, amendedStudy;
    private Amendment amendment0, amendment1, devAmendment;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private AmendmentService amendmentService;
    private DeltaService deltaService;

    private AmendedTemplateHelper helper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        studyDao = registerDaoMockFor(StudyDao.class);
        amendmentDao = registerDaoMockFor(AmendmentDao.class);
        amendmentService = registerMockFor(AmendmentService.class);
        deltaService = registerMockFor(DeltaService.class);

        request.getAttributes().put(
            UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), STUDY_NAME_ENCODED);

        study = createBasicTemplate();
        study.setName(STUDY_NAME);
        amendment0 = study.getAmendment();

        amendment1 = new Amendment();
        amendment1.setName("Amendment B");
        amendment1.setDate(createDate(2007, Calendar.OCTOBER, 19));
        study.pushAmendment(amendment1);

        devAmendment = createAmendment("Amendment C", createDate(2007, Calendar.DECEMBER, 12));
        study.setDevelopmentAmendment(devAmendment);

        amendedStudy = study.transientClone();

        helper = new AmendedTemplateHelper();
        helper.setStudyDao(studyDao);
        helper.setAmendmentDao(amendmentDao);
        helper.setAmendmentService(amendmentService);
        helper.setDeltaService(deltaService);
        helper.setRequest(request);
    }

    private void assertHelped(Study expected) {
        replayMocks();
        assertEquals(expected, helper.getAmendedTemplate());
        verifyMocks();
    }

    private void assertHelpFailed(String expectedMessage) {
        replayMocks();
        try {
            helper.getAmendedTemplate();
            fail("Exception not thrown");
        } catch (AmendedTemplateHelper.NotFound err) {
            assertEquals("Wrong error", expectedMessage, err.getMessage());
        }
        verifyMocks();
    }

    public void testGetWithEarlierAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), amendment0.getNaturalKey());

        expectFoundStudy();
        expectFoundAmendment(amendment0);
        expectAmendClonedStudy(amendment0);

        assertHelped(amendedStudy);
    }

    public void testGetCurrentAmendmentExplicitly() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), AMENDMENT_KEY_ENCODED);

        expectFoundStudy();
        expectFoundAmendment(amendment1);
        expectAmendClonedStudy(amendment1);
        assertHelped(amendedStudy);
    }

    public void testGetWithCurrentAmendmentIdentifier() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateHelper.CURRENT);

        expectFoundStudy();
        expectAmendClonedStudy(amendment1);
        assertHelped(amendedStudy);
    }

    public void testGetWithDevelopmentAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateHelper.DEVELOPMENT);

        expectFoundStudy();
        expectRevised(devAmendment);
        assertHelped(amendedStudy);
    }

    public void testNothingForDevelopmentWhenThereIsNoDevelopmentAmendment() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateHelper.DEVELOPMENT);
        study.setDevelopmentAmendment(null);

        expectFoundStudy();

        assertHelpFailed("Study template Mutant Study is not in development");
    }

    public void testNothingForNoStudyIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.STUDY_IDENTIFIER.attributeName(), "");
        assertHelpFailed("No study specified");
    }

    public void testNothingForNoAmendmentIdentifier() throws Exception {
        request.getAttributes().put(UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), "");
        expectFoundStudy();

        assertHelpFailed("No amendment specified");
    }

    public void testNothingForUnassociatedAmendment() throws Exception {
        Amendment other = new Amendment();
        other.setDate(DateUtils.createDate(2003, Calendar.MARCH, 1));
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(), other.getNaturalKey());

        expectFoundStudy();
        expectFoundAmendment(other);
        assertHelpFailed("The amendment 2003-03-01 is not part of Mutant Study");
    }

    public void testIsDevelopmentWhenIdentIsDev() throws Exception {
        UriTemplateParameters.AMENDMENT_IDENTIFIER.putIn(request, AmendedTemplateHelper.DEVELOPMENT);
        assertTrue(helper.isDevelopmentRequest());
    }

    public void testIsDevelopmentWhenIdentIsNull() throws Exception {
        UriTemplateParameters.AMENDMENT_IDENTIFIER.removeFrom(request);
        assertFalse(helper.isDevelopmentRequest());
    }

    public void testIsDevelopmentWhenIdentIsCurrent() throws Exception {
        UriTemplateParameters.AMENDMENT_IDENTIFIER.putIn(request, AmendedTemplateHelper.CURRENT);
        assertFalse(helper.isDevelopmentRequest());
    }
    
    public void testIsDevelopmentWhenIdentIsForAmendment() throws Exception {
        UriTemplateParameters.AMENDMENT_IDENTIFIER.putIn(request, amendment1.getNaturalKey());
        assertFalse(helper.isDevelopmentRequest());
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private PlanTreeNode doDrillDown(Class<? extends PlanTreeNode> target) {
        replayMocks();
        PlanTreeNode found = helper.drillDown(target);
        verifyMocks();
        return found;
    }

    public void testDrillDownToSegment() throws Exception {
        expectDrillDownIsPossible();

        UriTemplateParameters.EPOCH_NAME.putIn(request, "Treatment");
        UriTemplateParameters.STUDY_SEGMENT_NAME.putIn(request, "C");
        UriTemplateParameters.PERIOD_IDENTIFIER.putIn(request, "foom");

        Class<StudySegment> target = StudySegment.class;
        assertSame(amendedStudy.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(2),
            doDrillDown(target));
    }

    public void testDrillDownToSegmentWhenSegmentNotPresent() throws Exception {
        expectDrillDownIsPossible();

        UriTemplateParameters.EPOCH_NAME.putIn(request, "Treatment");
        UriTemplateParameters.STUDY_SEGMENT_NAME.putIn(request, "Seventy");
        UriTemplateParameters.PERIOD_IDENTIFIER.putIn(request, "foom");

        try {
            doDrillDown(StudySegment.class);
            fail("No exception");
        } catch (AmendedTemplateHelper.NotFound notFound) {
            assertEquals("No study segment identified by 'Seventy' in epoch", notFound.getMessage());
        }
    }

    public void testDrillDownToSegmentWhenEpochNotPresent() throws Exception {
        expectDrillDownIsPossible();

        UriTemplateParameters.EPOCH_NAME.putIn(request, "Phony");
        UriTemplateParameters.STUDY_SEGMENT_NAME.putIn(request, "C");
        UriTemplateParameters.PERIOD_IDENTIFIER.putIn(request, "foom");

        try {
            doDrillDown(StudySegment.class);
            fail("No exception");
        } catch (AmendedTemplateHelper.NotFound notFound) {
            assertEquals("No epoch identified by 'Phony' in planned calendar", notFound.getMessage());
        }
    }

    public void testDrillDownToPlannedActivity() throws Exception {
        expectDrillDownIsPossible();

        UriTemplateParameters.EPOCH_NAME.putIn(request, "Treatment");
        UriTemplateParameters.STUDY_SEGMENT_NAME.putIn(request, "C");
        UriTemplateParameters.PERIOD_IDENTIFIER.putIn(request, "foom");
        UriTemplateParameters.PLANNED_ACTIVITY_IDENTIFIER.putIn(request, "elab");

        StudySegment segment = amendedStudy.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(2);
        Period p = createPeriod("foom", 1, 35, 1);
        PlannedActivity pa = setGridId("elab", createPlannedActivity("barm", 8));
        p.addPlannedActivity(pa);
        segment.addPeriod(p);

        assertSame(pa, doDrillDown(PlannedActivity.class));
    }

    public void testGetReadAuthorizationsForReleasedTemplate() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateHelper.CURRENT);
        study.addSite(createSite("B", "Bi"));
        study.addSite(createSite("T", "Ti"));
        study.addManagingSite(createSite("M", "Mi"));

        expectFoundStudy();
        expectAmendClonedStudy(amendment1);

        replayMocks();
        Collection<ResourceAuthorization> actual = helper.getReadAuthorizations();
        verifyMocks();

        assertEquals("Wrong number of authorizations", 14, actual.size());
        // specifics are tested in ResourceAuthorizationTest
    }

    public void testGetReadAuthorizationsForDevelopmentTemplate() throws Exception {
        request.getAttributes().put(
            UriTemplateParameters.AMENDMENT_IDENTIFIER.attributeName(),
            AmendedTemplateHelper.DEVELOPMENT);
        study.addSite(createSite("B", "Bi"));
        study.addSite(createSite("T", "Ti"));
        study.addManagingSite(createSite("M", "Mi"));

        expectFoundStudy();
        expectRevised(devAmendment);

        replayMocks();
        Collection<ResourceAuthorization> actual = helper.getReadAuthorizations();
        verifyMocks();

        assertEquals("Wrong number of authorizations", 6, actual.size());
        // specifics are tested in ResourceAuthorizationTest
    }

    ////// EXPECTATIONS

    private void expectFoundStudy() {
        expect(studyDao.getByAssignedIdentifier(STUDY_NAME)).andReturn(study);
    }

    private void expectFoundAmendment(Amendment expected) {
        expect(amendmentDao.getByNaturalKey(expected.getNaturalKey(), study)).andReturn(expected);
    }

    private void expectAmendClonedStudy(Amendment target) {
        expect(amendmentService.getAmendedStudy(eq(study), eq(target))).andReturn(amendedStudy);
    }

    private void expectRevised(Amendment target) {
        expect(deltaService.revise(eq(study), eq(target))).andReturn(amendedStudy);
    }

    private void expectDrillDownIsPossible() {
        UriTemplateParameters.AMENDMENT_IDENTIFIER.putIn(request, AmendedTemplateHelper.DEVELOPMENT);

        expectFoundStudy();
        expectRevised(devAmendment);
    }
}