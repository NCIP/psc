package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRowDao extends ReportDao<ScheduledActivitiesReportRow>{

    public Class<ScheduledActivitiesReportRow> domainClass() {
        return ScheduledActivitiesReportRow.class;
    }
}
