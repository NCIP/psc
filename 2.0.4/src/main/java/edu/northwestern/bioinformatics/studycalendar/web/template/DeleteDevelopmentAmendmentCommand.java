package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Rhett Sutphin
 */
public class DeleteDevelopmentAmendmentCommand {
    private AmendmentService amendmentService;

    private Study study;

    public DeleteDevelopmentAmendmentCommand(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    public void apply() {
        amendmentService.deleteDevelopmentAmendment(study);
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
