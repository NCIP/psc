package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import static org.easymock.EasyMock.*;

/**
 * @author Nataliya Shurupova
 * @author Rhett Sutphin
 */
public class EditPopulationCommandTest extends StudyCalendarTestCase {
    private static final String STUDY_NAME = "NU-1066";

    private Population originalPopulation;

    private PopulationService populationService;
    private AmendmentService amendmentService;
    private Study study;

    private EditPopulationCommand command;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        originalPopulation = Fixtures.createPopulation("Abbreviation", "Name");
        amendmentService = registerMockFor(AmendmentService.class);
        populationService = registerMockFor(PopulationService.class);

        originalPopulation = Fixtures.createPopulation("Abbr", "name");
        originalPopulation.setId(10);
        Set<Population> pops = new HashSet<Population>();
        pops.add(originalPopulation);

        study = setId(100, Fixtures.createBasicTemplate());
        study.setAssignedIdentifier(STUDY_NAME);

        Amendment a2 = setId(2, createAmendments("A0", "A1", "A2"));
        Amendment a1 = setId(1, a2.getPreviousAmendment());

        study.setAmendment(a2);
        study.setDevelopmentAmendment(a1);
        study.setPopulations(pops);

        command = new EditPopulationCommand(originalPopulation, populationService, amendmentService, study);
    }

    public void testIsEdit() throws Exception {
        originalPopulation.setId(null);
        assertFalse("Population is not in edit state", command.isEdit());
    }

    public void testIsEditTrue() throws Exception {
        command.getPopulation().setId(25);
        assertTrue("Population is in edit state", command.isEdit());
    }

    public void testApply() throws Exception {
        List<Change> changes = new ArrayList<Change>();
        Change changeName = PropertyChange.create("name", originalPopulation.getName(), command.getPopulation().getName());
        Change changeAbbreviation = PropertyChange.create("abbreviation", originalPopulation.getAbbreviation(), command.getPopulation().getAbbreviation());
        changes.add(changeName);
        changes.add(changeAbbreviation);

        expect(amendmentService.updateDevelopmentAmendmentForStudyAndSave(originalPopulation, study, changes.toArray(new Change[changes.size()]))).andReturn(originalPopulation).anyTimes();

        replayMocks();
        command.apply();
        verifyMocks();
    }

    public void testAuthorizedForBuilder() throws Exception {
        study.addManagingSite(createSite("T", "T'"));

        replayMocks();
        Collection<ResourceAuthorization> actualAuths = command.authorizations(null);
        verifyMocks();

        assertEquals("Wrong number of authorizations", 1, actualAuths.size());
        ResourceAuthorization actual = actualAuths.iterator().next();
        assertEquals("Wrong role", PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER, actual.getRole());
        assertEquals("Wrong study", study.getAssignedIdentifier(), actual.getScope(ScopeType.STUDY));
        assertEquals("Wrong site", "T'", actual.getScope(ScopeType.SITE));
    }
}
