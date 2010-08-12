package edu.northwestern.bioinformatics.studycalendar.domain.reporting;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import gov.nih.nci.security.authorization.domainobjects.User;

/**
 * @author John Dzak
 */
public class ScheduledActivitiesReportRow implements DomainObject {
    private Integer id;
    private ScheduledActivity scheduledActivity;
    private Subject subject;
    private Study study;
    private Site site;
    private String studySubjectId;
    private Long responsibleUserCsmUserId;
    private User responsibleUser;

    ////// SEPARATELY RESOLVED PROPERTIES

    /*
      TODO: I would prefer that this field's type be PscUser.  However, that would introduce
      a dependency on psc:authorization from psc:domain.  psc:authorization's legacy mode support
      forces a dependency on psc:domain, so the reverse dependency isn't possible until legacy
      mode can be completely removed.
     */

    public User getResponsibleUser() {
        return responsibleUser;
    }

    public void setResponsibleUser(User responsibleUser) {
        this.responsibleUser = responsibleUser;
        if (responsibleUser != null) this.setResponsibleUserCsmUserId(responsibleUser.getUserId());
    }

    ////// BOUND PROPERTIES

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

    public Long getResponsibleUserCsmUserId() {
        return responsibleUserCsmUserId;
    }

    public void setResponsibleUserCsmUserId(Long responsibleUserCsmUserId) {
        this.responsibleUserCsmUserId = responsibleUserCsmUserId;
    }

    public String getStudySubjectId() {
        return studySubjectId;
    }

    public void setStudySubjectId(String studySubjectId) {
        this.studySubjectId = studySubjectId;
    }
}
