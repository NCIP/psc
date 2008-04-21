package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.utils.MutableRange;

import java.util.Date;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportFilters extends ReportFilters {
    private SubstringFilterLimit studyAssignedIdentifier = new SubstringFilterLimit("studyAssignedIdentifier");
    private SubstringFilterLimit siteName = new SubstringFilterLimit("siteName");
    private ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>> currentStateMode =
            new ControlledVocabularyObjectFilterLimit<ScheduledActivityMode<?>>("currentStateMode");
    private RangeFilterLimit<Date> actualActivityDate = new RangeFilterLimit<Date>("actualActivityDate");

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

    public String getSiteName() {
        return siteName.getValue();
    }

    public void setSiteName(String value) {
        siteName.setValue(value);
    }

    public MutableRange<Date> getActualActivityDate() {
        return actualActivityDate.getValue();
    }

    public void setActualActivityDate(MutableRange<Date> range) {
        this.actualActivityDate.setValue(range);
    }
}
