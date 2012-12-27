/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Child;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
@Transactional(readOnly = true)
public class PopulationService {
    private PopulationDao populationDao;
    private DeltaService deltaService;

    @Transactional(readOnly = false)
    public void lookupForPopulationUse(Population pop, Study study) {
        List<Population> populations = findAllPopulationsForDevelopmentStudy(study);
        populations.addAll(populationDao.getAllFor(study));
        if (populations.contains(pop)) {
            throw new StudyCalendarValidationException("The population is already used by study %s. Please select the different name and abbreviation.",
                study.getAssignedIdentifier());
        } else {
            List<String> names = new ArrayList<String>();
            List<String> abbreviations = new ArrayList<String>();
            for (Population population : populations) {
                names.add(population.getName());
                abbreviations.add(population.getAbbreviation());
            }
            if (names.contains(pop.getName())) {
                throw new StudyCalendarValidationException("The population name '%s' is already used by study %s. Please select the different name.",
                    pop.getName(), study.getAssignedIdentifier());
            } else if (abbreviations.contains(pop.getAbbreviation())) {
                throw new StudyCalendarValidationException("The population abbreviation '%s' is already used by study %s. Please select the different abbreviation.",
                    pop.getAbbreviation(), study.getAssignedIdentifier());
            }
        }
    }

    private List<Population> findAllPopulationsForDevelopmentStudy(Study study) {
        List<Population> populations =  new ArrayList<Population>();
        if (study.getDevelopmentAmendment() != null) {
            for (Delta<?> delta : study.getDevelopmentAmendment().getDeltas()) {
                for (Change change : delta.getChanges()) {
                    if (change instanceof ChildrenChange) {
                        Child child = deltaService.findChangeChild((ChildrenChange)change);
                        if (child instanceof Population) {
                            populations.add((Population)child);
                        }
                    }
                }
            }
        }
        return populations;
    }

    @Transactional(propagation = Propagation.SUPPORTS)
    public String suggestAbbreviation(Study study, String populationName) {
        Set<String> used = populationDao.getAbbreviations(study);
        List<Population> populations = findAllPopulationsForDevelopmentStudy(study);
        for (Population population : populations) {
            used.add(population.getAbbreviation());
        }
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

    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
