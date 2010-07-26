package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PropertyChange;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.PopulationService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class EditPopulationCommand implements Validatable, PscAuthorizedCommand {
    private Population population;
    private Population originalPopulation;

    private PopulationService populationService;
    private AmendmentService amendmentService;
    private Study study;

    public EditPopulationCommand(
        Population population, PopulationService populationService,
        AmendmentService amendmentService, Study study
    ) {
        this.originalPopulation = population;
        this.population = new Population();
        this.population.setName(population.getName());
        this.population.setAbbreviation(population.getAbbreviation());
        this.study = study;
        this.population.setStudy(null);
        this.populationService = populationService;
        this.amendmentService = amendmentService;

        if (this.populationService == null) throw new IllegalArgumentException("populationService required");
    }

    public boolean isEdit() {
        return originalPopulation.getId() != null;
    }

    public Collection<ResourceAuthorization> authorizations(Errors bindErrors) {
        return ResourceAuthorization.createTemplateManagementAuthorizations(
            study, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER);
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

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void validate(Errors errors) {
        if (population != null && population.getAbbreviation().contains(" ")) {
            errors.reject("error.population.contains.spaces");
        }
    }
}
