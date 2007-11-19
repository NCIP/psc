package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.AdverseEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;

/**
 * @author Rhett Sutphin
 */
public class ApiCommand {
    private String method;

    private Study study = new Study();
    private Subject subject = new Subject();
    private Site site = new Site();
    private AdverseEvent adverseEvent = new AdverseEvent();

    private ScheduledCalendarService scheduledCalendarService;

    public ApiCommand(ScheduledCalendarService scheduledCalendarService) {
        this.scheduledCalendarService = scheduledCalendarService;
    }

    public Object execute() {
        if ("registerSevereAdverseEvent".equals(getMethod())) {
            scheduledCalendarService.registerSevereAdverseEvent(
                getStudy(), getSubject(), getSite(), getAdverseEvent());
        } else {
            throw new IllegalArgumentException("This interface doesn't support the " + getMethod() + " method");
        }
        return null;
    }

    ////// BOUND PROPERTIES

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }

    public AdverseEvent getAdverseEvent() {
        return adverseEvent;
    }

    public void setAdverseEvent(AdverseEvent adverseEvent) {
        this.adverseEvent = adverseEvent;
    }
}
