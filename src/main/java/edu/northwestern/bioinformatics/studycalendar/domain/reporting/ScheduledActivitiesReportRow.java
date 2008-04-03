package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRow implements DomainObject {
    private Integer id;
    private ScheduledActivity scheduledActivity;
    private Subject subject;
    private Study study;
    private Site site;

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

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }
}
