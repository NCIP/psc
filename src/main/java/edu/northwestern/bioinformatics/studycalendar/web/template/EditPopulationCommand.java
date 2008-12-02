package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.dao.PopulationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class EditPopulationCommand {
    private Population population;
    private Population originalPopulation;


    private PopulationService populationService;
    private AmendmentService amendmentService;
    private PopulationDao populationDao;
    private Study study;


    private final Logger log = LoggerFactory.getLogger(getClass());

    public EditPopulationCommand(Population population, PopulationService populationService,
                                 AmendmentService amendmentService, PopulationDao populationDao, Study study) {
        this.originalPopulation = population;
        this.population = new Population();
        this.population.setName(population.getName());
        this.population.setAbbreviation(population.getAbbreviation());
        this.study = study;
        this.population.setStudy(null);
        this.populationService = populationService;
        this.amendmentService = amendmentService;
        this.populationDao = populationDao;

        if (this.populationService == null) throw new IllegalArgumentException("populationService required");
    }

    public boolean isEdit() {
        return population.getId() != null;
    }

    public void apply() {
        if (getStudy().isInDevelopment()){
            Change change = null;
            List<Change> changes = new ArrayList<Change>();
            if (originalPopulation.getId() == null) {
                change = Add.create(population);
                amendmentService.updateDevelopmentAmendmentForStudyAndSave(getStudy(), change);
            } else {
                if(population.getName() !=null && population.getAbbreviation()!=null) {
                    Change changeName = PropertyChange.create("name", originalPopulation.getName(), population.getName());
                    Change changeAbbreviation = PropertyChange.create("abbreviation", originalPopulation.getAbbreviation(), population.getAbbreviation());
                    changes.add(changeName);
                    changes.add(changeAbbreviation);
                }
                amendmentService.updateDevelopmentAmendmentForStudyAndSave(originalPopulation, getStudy(), changes.toArray(new Change[changes.size()]));
            }
        } else {
            populationService.savePopulation(population);
        }
    }

    ////// BOUND PROPERTIES

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }


    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
