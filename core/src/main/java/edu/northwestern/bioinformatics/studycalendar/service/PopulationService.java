package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PopulationService {
    private PopulationDao populationDao;

    @Transactional(readOnly = false)
    public Population lookupAndSuggestAbbreviation(Population pop, Study study) {
        if (pop.getAbbreviation() == null) {
            pop.setAbbreviation(suggestAbbreviation(study, pop.getName()));
        } else {
            // ensure that the abbreviation isn't already in use in this study
            Population match = populationDao.getByAbbreviation(study, pop.getAbbreviation());
            if (match != null && !match.equals(pop)) {
                throw new StudyCalendarValidationException("%s is already using the abbreviation '%s'",
                        match.getName(), match.getAbbreviation());
            }
        }
        return pop;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public String suggestAbbreviation(Study study, String populationName) {
        Set<String> used = populationDao.getAbbreviations(study);
        // First option: first character
        String option = populationName.substring(0, 1);
        if (!used.contains(option)) return option;
        // Second option: first character of first word + first character of second word
        String[] words = populationName.split("\\s+", 2);
        if (words.length > 1) {
            option = words[0].substring(0, 1) + words[1].substring(0, 1);
            if (!used.contains(option)) return option;
        }
        // Third option: first two characters of first word
        option = populationName.substring(0, 2);
        if (!used.contains(option)) return option;
        // All later options:  first character of first word plus a serial number
        int serial = 1;
        String first = populationName.substring(0, 1);
        while (true) {
            option = first + serial;
            if (!used.contains(option)) return option;
            serial++;
        }
    }

    @Transactional(readOnly = false)
    public void delete(final Set<Population> populations) {
        for (Population population : populations) {
            populationDao.delete(population);
        }
    }

    ////// CONFIGURATION

    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }


}
