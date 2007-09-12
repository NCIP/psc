package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
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

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
