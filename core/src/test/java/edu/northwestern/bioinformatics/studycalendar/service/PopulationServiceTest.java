package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import gov.nih.nci.cabig.ctms.lang.DateTools;

import static org.easymock.classextension.EasyMock.*;

import java.util.Calendar;
import java.util.TreeSet;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class PopulationServiceTest extends StudyCalendarTestCase {
    private Population population;
    private PopulationService service;
    private PopulationDao populationDao;
    private Study study;
    private DeltaService deltaService;
    final private String NAME = "Child-bearing females";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        population = new Population();
        population = createNamedInstance(NAME, Population.class);
        study = createBasicTemplate();
        study.setAssignedIdentifier("TestStudy");
        population.setStudy(study);

        populationDao = registerDaoMockFor(PopulationDao.class);
        deltaService = registerMockFor(DeltaService.class);
        service = new PopulationService();
        service.setPopulationDao(populationDao);
        service.setDeltaService(deltaService);
    }

    public void testLookupForPopulationUseWhenPopulationAlreadyInStudy() throws Exception {
        population.setAbbreviation("FMs");
        expect(populationDao.getAllFor(study)).andReturn(Arrays.asList(population));
        replayMocks();
        try {
            service.lookupForPopulationUse(population, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            String expectedMsg = "The population is already used by study TestStudy. Please select the different name and abbreviation.";
            assertEquals("Wrong message", expectedMsg, scve.getMessage());
        }
        verifyMocks();
    }

    public void testLookupForPopulationUseWhenPopulationNameAlreadyInStudy() throws Exception {
        population.setAbbreviation("FMs");
        expect(populationDao.getAllFor(study)).andReturn(Arrays.asList(population));
        Population pop = createPopulation("Abbr", NAME);
        replayMocks();
        try {
            service.lookupForPopulationUse(pop, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            String expectedMsg = "The population name 'Child-bearing females' is already used by study TestStudy. Please select the different name.";
            assertEquals("Wrong message", expectedMsg, scve.getMessage());
        }
        verifyMocks();
    }

    public void testLookupForPopulationUseWhenPopulationAbbreviationAlreadyInStudy() throws Exception {
        population.setAbbreviation("FMs");
        expect(populationDao.getAllFor(study)).andReturn(Arrays.asList(population));
        Population pop = createPopulation("FMs", "TestPopulation");
        replayMocks();
        try {
            service.lookupForPopulationUse(pop, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            String expectedMsg = "The population abbreviation 'FMs' is already used by study TestStudy. Please select the different abbreviation.";
            assertEquals("Wrong message", expectedMsg, scve.getMessage());
        }
        verifyMocks();
    }

    public void testLookupForPopulationWhenStudyHasDevelopmentAmendment() throws Exception {
        Amendment amendment = Fixtures.createInDevelopmentAmendment("Amendment", DateTools.createDate(2010, Calendar.NOVEMBER, 12), true);
        Delta<Study> delta = Delta.createDeltaFor(study);
        Population pop = createPopulation("Abbr", "TestPopulation");
        Add add = Add.create(pop);
        delta.addChange(add);
        amendment.addDelta(delta);
        study.setDevelopmentAmendment(amendment);
        expect(populationDao.getAllFor(study)).andReturn(Arrays.asList(population));
        expect(deltaService.findChangeChild(add)).andReturn(pop);
        Population pop1 = createPopulation("Abbr", "TestPopulation");
        replayMocks();
        try {
            service.lookupForPopulationUse(pop1, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException scve) {
            String expectedMsg = "The population is already used by study TestStudy. Please select the different name and abbreviation.";
            assertEquals("Wrong message", expectedMsg, scve.getMessage());
        }
        verifyMocks();
    }

    public void testFirstSuggestionIsFirstLetterOfFirstWord() throws Exception {
        assertAbbreviationSuggested("C", NAME);
    }

    public void testSecondSuggestionIsFirstLetterOfFirstWordPlusFirstLetterOfSecondWord() throws Exception {
        assertAbbreviationSuggested("Cf", NAME, "C");
    }

    public void testThirdSuggestionIsFirstLetterOfFirstWordPlusSecondLetterOfFirstWord() throws Exception {
        assertAbbreviationSuggested("Ch", NAME, "C", "Cf");
    }

    public void testLaterSuggestionsAreFirstLetterPlusANumber() throws Exception {
        assertAbbreviationSuggested("C1", NAME, "C", "Cf", "Ch");
        assertAbbreviationSuggested("C3", NAME, "C", "Cf", "Ch", "C1", "C2");
        assertAbbreviationSuggested("C10", NAME,
            "C", "Cf", "Ch", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9");
        assertAbbreviationSuggested("C11", NAME,
            "C", "Cf", "Ch", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9", "C10");
    }

    public void testSecondSuggestionIsFirstLetterOfFirstWordPlusSecondLetterOfFirstWordIfOneWordOnly() throws Exception {
        assertAbbreviationSuggested("He", "Hepatitis-positive", "H");
    }

    private void assertAbbreviationSuggested(String expectedAbbreviation, String populationName, String... existingAbbreviations) {
        expect(populationDao.getAbbreviations(study)).andReturn(new TreeSet<String>(Arrays.asList(existingAbbreviations)));
        replayMocks();
        assertEquals("Wrong suggestion", expectedAbbreviation,
            service.suggestAbbreviation(study, populationName));
        verifyMocks();
        resetMocks();
    }
}
