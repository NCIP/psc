package edu.northwestern.bioinformatics.studycalendar.web.reporting;

import edu.northwestern.bioinformatics.studycalendar.dao.reporting.ScheduledActivitiesReportFilters;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportCommand {
    ScheduledActivitiesReportFilters filters;
    String label;

    public ScheduledActivitiesReportCommand(ScheduledActivitiesReportFilters filters) {
        this.filters = filters;
    }

    public ScheduledActivitiesReportFilters getFilters() {
        return filters;
    }

    public void setFilters(ScheduledActivitiesReportFilters filters) {
        this.filters = filters;
    }


    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setLabelFilter() {
        filters.setLabel(getLabel());
    }
}
