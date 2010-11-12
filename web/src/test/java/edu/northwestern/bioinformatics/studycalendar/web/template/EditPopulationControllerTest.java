package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.*;

/**
 * @author Nataliya Shurupova
 */
public class EditPopulationControllerTest extends ControllerTestCase {
    private static final String STUDY_NAME = "NU-1066";

    private StudyDao studyDao;

    private AmendmentService amendmentService;

    private EditPopulationController controller;
    private EditPopulationCommand command;
    private PopulationService populationService;
    private Population originalPopulation;
    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        amendmentService = registerMockFor(AmendmentService.class);
        studyDao = registerDaoMockFor(StudyDao.class);
        populationService = registerMockFor(PopulationService.class);
        PopulationDao populationDao = registerDaoMockFor(PopulationDao.class);

        controller = new EditPopulationController();
        controller.setControllerTools(controllerTools);
        controller.setAmendmentService(amendmentService);
        controller.setPopulationService(populationService);

        controller.setPopulationDao(populationDao);
        controller.setStudyDao(studyDao);

        Set<Population> pops = new HashSet<Population>();
        originalPopulation = Fixtures.createPopulation("Abbr", "name");
        originalPopulation.setId(10);
        pops.add(originalPopulation);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setAssignedIdentifier(STUDY_NAME);
        Amendment a2 = setId(2, createAmendments("A0", "A1", "A2"));
        Amendment a1 = setId(1, a2.getPreviousAmendment());
        study.setAmendment(a2);
        study.setDevelopmentAmendment(a1);
        study.setPopulations(pops);
        command = new EditPopulationCommand(originalPopulation, populationService, amendmentService, study);

        expect(populationDao.getById(10)).andReturn(originalPopulation).anyTimes();
        expect(studyDao.getById(100)).andReturn(study).anyTimes();
    }

    public void testCommandForRegularNewPopulation() throws Exception {
        request.setParameter("study", study.getId().toString());
        replayMocks();
        Object actual = controller.formBackingObject(request);
        verifyMocks();
        assertTrue(actual instanceof EditPopulationCommand);
    }

    @SuppressWarnings({"unchecked"})
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
        populationService.lookupForPopulationUse(command.getPopulation(), study);
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
