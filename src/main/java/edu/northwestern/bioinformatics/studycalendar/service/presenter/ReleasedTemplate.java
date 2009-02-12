package edu.northwestern.bioinformatics.studycalendar.service.presenter;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;

/**
 * @author Rhett Sutphin
*/
public class ReleasedTemplate {
    private Study study;
    private boolean canAssignSubjects;

    public ReleasedTemplate(Study study, boolean canAssignSubjects) {
        this.study = study;
        this.canAssignSubjects = canAssignSubjects;
    }

    public boolean getCanAssignSubjects() {
        return canAssignSubjects;
    }

    public int getId() {
        return study.getId();
    }

    public String getDisplayName() {
        StringBuilder sb = new StringBuilder(study.getName());
        if (study.isAmended()) {
            sb.append(" [").append(study.getAmendment().getDisplayName()).append(']');
        }
        return sb.toString();
    }

    public Study getStudy() {
        return study;
    }
}
