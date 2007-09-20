package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
public class AmendmentService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyService studyService;
    private DeltaService deltaService;
    private TemplateService templateService;

    /**
     * Commit the changes in the developmentAmendment for the given study.  This means:
     * <ul>
     *   <li>Apply the deltas to the persistent calendar</li>
     *   <li>Move the development amendment to the study's amendment stack</li>
     *   <li>Save it all</li>
     * </ul>
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void amend(Study source) {
        Amendment dev = source.getDevelopmentAmendment();
        if (dev == null) {
            throw new StudyCalendarSystemException("%s has no development amendment", source);
        }
        deltaService.apply(source, dev);
        dev.setPreviousAmendment(source.getAmendment());
        source.setAmendment(dev);
        source.setDevelopmentAmendment(null);
        studyService.save(source);
    }

    /**
     * Takes the provided source study and rolls it back to the amendment
     */
    public Study getAmendedStudy(Study source, Amendment target) {
        if (!(source.getAmendment().equals(target) || source.getAmendment().hasPreviousAmendment(target))) {
            throw new StudyCalendarSystemException(
                "Amendment %s (%s) does not apply to the template for %s (%s)",
                target.getName(), target.getGridId(), source.getName(), source.getGridId());
        }

        Study amended = source.transientClone();
        while (!target.equals(amended.getAmendment())) {
            log.debug("Rolling {} back to {}", source, amended.getAmendment().getPreviousAmendment().getName());
            deltaService.revert(amended, amended.getAmendment());
            amended.setAmendment(amended.getAmendment().getPreviousAmendment());
        }

        return amended;
    }

    /**
     * Finds the current development amendment for the study associated with the node
     * and merges in the given change.
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    public void updateDevelopmentAmendment(PlanTreeNode<?> node, Change change) {
        Study study = templateService.findAncestor(node, PlannedCalendar.class).getStudy();
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException("The study %s is not open for editing or amending", study);
        }
        deltaService.updateRevision(study.getDevelopmentAmendment(), node, change);
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
