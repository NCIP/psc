package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service provides orchestration methods for incrementally building up templates.
 * Methods should be added here for complex template modifications (more than just
 * adding/removing/moving/updating single nodes).  For simpler things, you can just
 * directly invoke {@link AmendmentService#updateDevelopmentAmendment}. 
 *
 * @see TemplateService
 * @author Rhett Sutphin
 */
@Transactional(readOnly = false)
public class TemplateDevelopmentService {
    private TemplateService templateService;
    private AmendmentService amendmentService;
    private DeltaService deltaService;

    /**
     * Copies the given period to the target segment (by updating the development amendment for
     * the study to which the target segment belongs).
     * @param source
     * @param target
     * @return the newly created copy period.  It will have already been added to <code>target</code>.
     */
    public Period copyPeriod(Period source, StudySegment target) {
        Study sourceStudy = templateService.findStudy(source);
        Study targetStudy = templateService.findStudy(target);
        boolean intraStudy = sourceStudy.equals(targetStudy);

        if (intraStudy) {
            source = deltaService.revise(source);
        }

        Period copy = source.clone();
        copy.clearIds();

        // convert Populations to comments
        if (!intraStudy) {
            for (PlannedActivity pa : copy.getPlannedActivities()) {
                updateCopiedPlannedActivity(pa, sourceStudy);
            }
        }

        amendmentService.updateDevelopmentAmendment(target, Add.create(copy));
        return copy;
    }

    private void updateCopiedPlannedActivity(PlannedActivity pa, Study sourceStudy) {
        if (pa.getPopulation() != null) {
            String msg = createCopiedWithPopulationMessage(sourceStudy, pa);
            if (pa.getDetails() == null) {
                pa.setDetails(msg);
            } else {
                pa.setDetails(
                    new StringBuilder(pa.getDetails()).append(" (").append(msg).append(')').toString()
                );
            }
            pa.setPopulation(null);
        }
    }

    private String createCopiedWithPopulationMessage(Study study, PlannedActivity pa) {
        return new StringBuilder().append("Copied from ")
            .append(study.getAssignedIdentifier())
            .append(", where it was restricted to ")
            .append(pa.getPopulation().getAbbreviation())
            .append(": ")
            .append(pa.getPopulation().getName())
            .toString();
    }

    ////// CONFIGURATION

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
