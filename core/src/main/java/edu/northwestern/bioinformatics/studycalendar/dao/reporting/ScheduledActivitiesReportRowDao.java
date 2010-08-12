package edu.northwestern.bioinformatics.studycalendar.dao.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.reporting.ScheduledActivitiesReportRow;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDao extends ReportDao<ScheduledActivitiesReportFilters, ScheduledActivitiesReportRow> {
    @Override
    public Class<ScheduledActivitiesReportRow> domainClass() {
        return ScheduledActivitiesReportRow.class;
    }
}
