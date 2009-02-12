package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Jaron Sampson
 */
public class ReleaseAmendmentCommand {
    private Study study;

    private AmendmentService amendmentService;

    public ReleaseAmendmentCommand(AmendmentService deltaService) {
        this.amendmentService = deltaService;
    }

    ////// LOGIC

    public void apply() {
        amendmentService.amend(getStudy());
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }
}
