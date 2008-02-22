package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;

/**
 * @author Rhett Sutphin
 */
public class EditPopulationCommand {
    private Population population;
    private PopulationService populationService;

    public EditPopulationCommand(Population population, PopulationService populationService) {
        this.population = population;
        this.populationService = populationService;
        if (this.populationService == null) throw new IllegalArgumentException("populationService required");
    }

    public boolean isEdit() {
        return population.getId() != null;
    }

    public void apply() {
        populationService.savePopulation(population);
    }

    ////// BOUND PROPERTIES

    public Population getPopulation() {
        return population;
    }

    public void setPopulation(Population population) {
        this.population = population;
    }
}
