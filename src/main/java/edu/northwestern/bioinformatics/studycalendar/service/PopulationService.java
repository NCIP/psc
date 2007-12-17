package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;

import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class PopulationService {
    private PopulationDao populationDao;

    public void createPopulation(Population pop) {
        if (pop.getAbbreviation() == null) {
            pop.setAbbreviation(suggestAbbreviation(pop.getStudy(), pop.getName()));
        } else {
            // ensure that the abbreviation isn't already in use in this study
            Population match = populationDao.getByAbbreviation(pop.getStudy(), pop.getAbbreviation());
            if (match != null) {
                throw new StudyCalendarValidationException("%s is already using the abbreviation '%s'",
                    match.getName(), match.getAbbreviation());
            }
        }
        populationDao.save(pop);
    }

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

    ////// CONFIGURATION

    public void setPopulationDao(PopulationDao populationDao) {
        this.populationDao = populationDao;
    }
}
