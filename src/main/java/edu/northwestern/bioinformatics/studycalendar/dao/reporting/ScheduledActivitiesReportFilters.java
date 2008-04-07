package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportFilters extends ReportFilters {
    private SubstringFilterLimit studyAssignedIdentifier = new SubstringFilterLimit("studyAssignedIdentifier");
    private ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>> currentStateMode =
            new ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>>("currentStateMode");

    protected String getHibernateFilterPrefix() {
        return "filter_";
    }

    public String getStudyAssignedIdentifier() {
        return studyAssignedIdentifier.getValue();
    }

    public void setStudyAssignedIdentifier(String value) {
        studyAssignedIdentifier.setValue(value);
    }

    public void setCurrentStateMode(ScheduledActivityMode<?> mode) {
        currentStateMode.setValue(mode);
    }

    public ScheduledActivityMode<?> getCurrentStateMode() {
        return currentStateMode.getValue();
    }
}
