package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRow implements DomainObject {
    Integer id;
    private ScheduledActivity scheduledActivity;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public ScheduledActivity getScheduledActivity() {
        return scheduledActivity;
    }

    public void setScheduledActivity(ScheduledActivity scheduledActivity) {
        this.scheduledActivity = scheduledActivity;
    }
}
