package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import static org.easymock.classextension.EasyMock.*;

import java.util.Collections;
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

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        population = new Population();
        population = createNamedInstance("Child-bearing females", Population.class);
        study = createBasicTemplate();
        population.setStudy(study);

        populationDao = registerDaoMockFor(PopulationDao.class);

        service = new PopulationService();
        service.setPopulationDao(populationDao);
    }

    public void testPopulationWithAbbreviationKeepsIt() throws Exception {
        population.setAbbreviation("FMs");
        expect(populationDao.getByAbbreviation(study, "FMs")).andReturn(null);

        replayMocks();
        service.lookupAndSuggestAbbreviation(population, study);
        verifyMocks();

        assertEquals("Abbreviation not preserved", "FMs", population.getAbbreviation());
    }

    public void testPopulationWithAlreadyUsedAbbreviationThrowsException() throws Exception {
        Population competitor = createNamedInstance("Friendly marionettes", Population.class);
        competitor.setAbbreviation("FMs");
        population.setAbbreviation("FMs");
        expect(populationDao.getByAbbreviation(study, "FMs")).andReturn(competitor);

        replayMocks();
        try {
            service.lookupAndSuggestAbbreviation(population, study);
            fail("Exception not thrown");
        } catch (StudyCalendarValidationException exception) {
            assertEquals("Wrong message", "Friendly marionettes is already using the abbreviation 'FMs'", exception.getMessage());
        }
        verifyMocks();
    }

    public void testPopulationWithoutAbbreviationUsesSuggested() throws Exception {
        expect(populationDao.getAbbreviations(study)).andReturn(Collections.<String>emptySet());
        replayMocks();
        service.lookupAndSuggestAbbreviation(population, study);
        verifyMocks();

        assertEquals("Abbreviation not set", "C", population.getAbbreviation());
    }

    public void testFirstSuggestionIsFirstLetterOfFirstWord() throws Exception {
        assertAbbreviationSuggested("C", "Child-bearing females");
    }

    public void testSecondSuggestionIsFirstLetterOfFirstWordPlusFirstLetterOfSecondWord() throws Exception {
        assertAbbreviationSuggested("Cf", "Child-bearing females", "C");
    }

    public void testThirdSuggestionIsFirstLetterOfFirstWordPlusSecondLetterOfFirstWord() throws Exception {
        assertAbbreviationSuggested("Ch", "Child-bearing females", "C", "Cf");
    }

    public void testLaterSuggestionsAreFirstLetterPlusANumber() throws Exception {
        assertAbbreviationSuggested("C1", "Child-bearing females", "C", "Cf", "Ch");
        assertAbbreviationSuggested("C3", "Child-bearing females", "C", "Cf", "Ch", "C1", "C2");
        assertAbbreviationSuggested("C10", "Child-bearing females",
            "C", "Cf", "Ch", "C1", "C2", "C3", "C4", "C5", "C6", "C7", "C8", "C9");
        assertAbbreviationSuggested("C11", "Child-bearing females",
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
