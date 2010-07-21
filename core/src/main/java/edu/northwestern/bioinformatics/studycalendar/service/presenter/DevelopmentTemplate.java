package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
*/
public class DevelopmentTemplate {
    private Study study;

    public DevelopmentTemplate(Study study) {
        this.study = study;
    }

    public int getId() {
        return study.getId();
    }

    public int getDevelopmentAmendmentId() {
        return study.getDevelopmentAmendment().getId();
    }

    public String getDisplayName() {
        return study.getDevelopmentDisplayName();
    }
}
