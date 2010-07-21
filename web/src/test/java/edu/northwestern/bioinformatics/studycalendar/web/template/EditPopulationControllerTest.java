package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createAmendments;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createInDevelopmentBasicTemplate;

import java.util.*;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Nataliya Shurupova
 */
public class EditPopulationControllerTest extends ControllerTestCase {
    private PopulationDao populationDao;
    private StudyDao studyDao;

    private PopulationService populationService;
    private AmendmentService amendmentService;
    private DeltaService deltaService;
    private TemplateService templateService;

    private EditPopulationController controller;
    private EditPopulationCommand command;

    private Population originalPopulation;
    private Study study;
    private static final String STUDY_NAME = "NU-1066";
    private Amendment a1, a2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        templateService = registerMockFor(TemplateService.class);
        populationService = registerMockFor(PopulationService.class);
        deltaService = registerMockFor(DeltaService.class);
        populationDao = registerDaoMockFor(PopulationDao.class);
        studyDao = registerDaoMockFor(StudyDao.class);

        controller = new EditPopulationController();
        controller.setControllerTools(controllerTools);
        controller.setAmendmentService(amendmentService);
        controller.setPopulationService(populationService);
        controller.setDeltaService(deltaService);
        controller.setTemplateService(templateService);

        controller.setPopulationDao(populationDao);
        controller.setStudyDao(studyDao);

        Set<Population> pops = new HashSet<Population>();
        originalPopulation = Fixtures.createPopulation("Abbr", "name");
        originalPopulation.setId(10);
        pops.add(originalPopulation);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setName(STUDY_NAME);
        a2 = setId(2, createAmendments("A0", "A1", "A2"));
        a1 = setId(1, a2.getPreviousAmendment());
        study.setAmendment(a2);
        study.setDevelopmentAmendment(a1);
        study.setPopulations(pops);
        command = new EditPopulationCommand(originalPopulation, populationService, amendmentService, populationDao, study);

        expect(populationDao.getById(10)).andReturn(originalPopulation).anyTimes();
        expect(studyDao.getById(100)).andReturn(study).anyTimes();
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, STUDY_CALENDAR_TEMPLATE_BUILDER);
    }

    public void testCommandForRegularNewPopulation() throws Exception {
        request.setParameter("study", study.getId().toString());
        replayMocks();
        Object actual = controller.formBackingObject(request);
        verifyMocks();
        assertTrue(actual instanceof EditPopulationCommand);
    }

    public void testRefData() throws Exception {
        Map<String, Object> refdata = controller.referenceData(request, command, null);
        assertNotNull("Study is Null", refdata.get("study"));
        assertNotNull("Amendment key is Null", refdata.keySet().contains("amendment"));
    }

    public void testApply() throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Change changeName = PropertyChange.create("name", originalPopulation.getName(), command.getPopulation().getName());
        Change changeAbbreviation = PropertyChange.create("abbreviation", originalPopulation.getAbbreviation(), command.getPopulation().getAbbreviation());
        changes.add(changeName);
        changes.add(changeAbbreviation);

        expect(amendmentService.updateDevelopmentAmendmentForStudyAndSave(originalPopulation, study, changes.toArray(new Change[changes.size()]))).andReturn(originalPopulation).anyTimes();

        replayMocks();
        ModelAndView mv = controller.onSubmit(request, response, command, null);
        verifyMocks();

        assertEquals("Wrong study id in the model ",100,  mv.getModel().get("study"));
        assertEquals("Wrong amendment id in the model ",1,  mv.getModel().get("amendment"));
        assertEquals("Wrong redirect view ","redirectToCalendarTemplate",  mv.getViewName());
    }

    public void testEditPopulationForInitialTemplate() throws Exception {
        Study devStudy = setId(101,createInDevelopmentBasicTemplate("InitialTemplate"));
        expect(studyDao.getById(101)).andReturn(devStudy).anyTimes();
        request.setParameter("population", originalPopulation.getId().toString());
        request.setParameter("study", devStudy.getId().toString());
        replayMocks();
        Object actual = controller.formBackingObject(request);
        verifyMocks();
        assertTrue(actual instanceof EditPopulationCommand);
    }
}
