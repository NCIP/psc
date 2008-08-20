package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.nwu.bioinformatics.commons.DateUtils;
import static edu.nwu.bioinformatics.commons.DateUtils.createDate;
import static org.easymock.EasyMock.*;

import java.util.Calendar;

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
    }

    private void assertHelped(Study expected) {
        replayMocks();
        assertEquals(expected, helper.getAmendedTemplate(request));
        verifyMocks();
    }

    private void assertHelpFailed(String expectedMessage) {
        replayMocks();
        try {
            helper.getAmendedTemplate(request);
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
}