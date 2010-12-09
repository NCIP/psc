package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.NotificationService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

/**
 * @author Rhett Sutphin
 */
@Transactional
public class DefaultScheduledCalendarService implements ScheduledCalendarService {
    private SubjectDao subjectDao;
    private SubjectService subjectService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private StudySegmentDao studySegmentDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private NotificationService notificationService;
    private ApplicationSecurityManager applicationSecurityManager;

    public ScheduledCalendar assignSubject(
            Study study, Subject subject, Site site, StudySegment firstStudySegment, Date startDate, String assignmentGridId
    ) {
        ParameterLoader loader = new ParameterLoader(study, site, firstStudySegment);

        Subject loadedSubject = load(subject, subjectDao, false);
        if (loadedSubject == null) {
            subjectDao.save(subject);
            loadedSubject = subject;
        } else {
            StudySubjectAssignment assignment = subjectDao.getAssignment(
                    loadedSubject, loader.getStudy(), loader.getSite());
            if (assignment != null) {
                throw new IllegalArgumentException(
                        "Subject already assigned to this study.  " +
                                "Use scheduleNextStudySegment to change to the next studySegment.");
            }
        }

        StudySite join = loader.validateSiteInStudy();
        loader.validateStudySegmentInStudy();

        PscUser user = applicationSecurityManager.getUser();
        StudySubjectAssignment newAssignment = subjectService.assignSubject(
                loadedSubject, join, loader.getStudySegment(), startDate, assignmentGridId, null, user);
        return newAssignment.getScheduledCalendar();
    }

    public ScheduledCalendar getScheduledCalendar(Study study, Subject subject, Site site) {
        ParameterLoader loader = new ParameterLoader(study, subject, site);

        StudySubjectAssignment assignment = loader.findAssignment();
        if (assignment == null) {
            return null;
        } else {
            ScheduledCalendar calendar = assignment.getScheduledCalendar();
            scheduledCalendarDao.initialize(calendar);
            return calendar;
        }
    }

    public Collection<ScheduledActivity> getScheduledActivities(
            Study study, Subject subject, Site site, Date startDate, Date endDate
    ) {
        ParameterLoader loader = new ParameterLoader(study, subject, site);
        ScheduledCalendar calendar = loader.findAssignment().getScheduledCalendar();
        return scheduledActivityDao.getEventsByDate(calendar, startDate, endDate);
    }

    public ScheduledActivity changeEventState(ScheduledActivity event, ScheduledActivityState newState) {
        ParameterLoader loader = new ParameterLoader(event);

        loader.getScheduledActivity().changeState(newState);
        scheduledActivityDao.save(loader.getScheduledActivity());
        return loader.getScheduledActivity();
    }

    public void scheduleNextStudySegment(
            Study study, Subject subject, Site site, StudySegment nextStudySegment, NextStudySegmentMode mode, Date startDate
    ) {
        ParameterLoader loader = new ParameterLoader(study, subject, site, nextStudySegment);
        subjectService.scheduleStudySegment(loader.findAssignment(), loader.getStudySegment(), startDate, mode);
    }

    public void registerSevereAdverseEvent(Study study, Subject subject, Site site, AdverseEvent adverseEvent) {
        ParameterLoader loader = new ParameterLoader(study, subject, site);
        StudySubjectAssignment assignment = loader.findAssignment();
        if (assignment == null) {
            throw new IllegalArgumentException("Subject is not assigned to this study at this site");
        }
        registerAeInternal(assignment, adverseEvent);
    }

    public void registerSevereAdverseEvent(StudySubjectAssignment assignment, AdverseEvent adverseEvent) {
        StudySubjectAssignment loadedAssignment = load(assignment, studySubjectAssignmentDao);
        registerAeInternal(loadedAssignment, adverseEvent);
    }

    private void registerAeInternal(StudySubjectAssignment assignment, AdverseEvent adverseEvent) {
        Notification notification = new Notification(adverseEvent);
        assignment.addAeNotification(notification);
        subjectDao.save(assignment.getSubject());
        User user = notification.getAssignment().getStudySubjectCalendarManager();
        if (user != null) {
            notificationService.sendNotificationMailToUsers(notification.getTitle(), notification.getMessage(), Arrays.asList(user.getEmailId()));
        }
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudySegmentDao(StudySegmentDao studySegmentDao) {
        this.studySegmentDao = studySegmentDao;
    }

    @Required
    public void setScheduledCalendarDao(ScheduledCalendarDao scheduledCalendarDao) {
        this.scheduledCalendarDao = scheduledCalendarDao;
    }

    @Required
    public void setScheduledActivityDao(ScheduledActivityDao scheduledActivityDao) {
        this.scheduledActivityDao = scheduledActivityDao;
    }

    @Required
    public void setStudySubjectAssignmentDao(StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    //////

    private <T extends MutableDomainObject> T load(T parameter, StudyCalendarMutableDomainObjectDao<T> dao) {
        return load(parameter, dao, true);
    }

    private <T extends MutableDomainObject> T load(T parameter, StudyCalendarMutableDomainObjectDao<T> dao, boolean required) {
        checkForGridId(parameter);
        T loaded = dao.getByGridId(parameter);
        if (required && loaded == null) {
            throw new IllegalArgumentException("No " + parameter.getClass().getSimpleName().toLowerCase() +
                    " with gridId " + parameter.getGridId());
        }
        return loaded;
    }

    private void checkForGridId(GridIdentifiable gridIdentifiable) {
        if (!gridIdentifiable.hasGridId()) {
            throw new IllegalArgumentException(
                    "No gridId on " + gridIdentifiable.getClass().getSimpleName().toLowerCase() + " parameter");
        }
    }

    private class ParameterLoader {
        private Study study;
        private Subject subject;
        private Site site;
        private StudySegment studySegment;
        private ScheduledActivity scheduledActivity;

        public ParameterLoader(Study study, Subject subject, Site site) {
            loadStudy(study);
            loadSubject(subject);
            loadSite(site);
        }

        public ParameterLoader(Study study, Subject subject, Site site, StudySegment studySegment) {
            loadStudy(study);
            loadSubject(subject);
            loadSite(site);
            loadStudySegment(studySegment);
        }

        public ParameterLoader(Study study, Site site, StudySegment studySegment) {
            loadStudy(study);
            loadSite(site);
            loadStudySegment(studySegment);
        }

        public ParameterLoader(ScheduledActivity scheduledActivity) {
            loadEvent(scheduledActivity);
        }

        ////// LOADING

        private void loadStudy(Study parameterStudy) {
            this.study = load(parameterStudy, studyDao);
        }

        private void loadSite(Site parameterSite) {
            this.site = load(parameterSite, siteDao);
        }

        private void loadSubject(Subject parameterSubject) {
            this.subject = load(parameterSubject, subjectDao);
        }

        private void loadStudySegment(StudySegment parameterStudySegment) {
            this.studySegment = parameterStudySegment == null ? null : load(parameterStudySegment, studySegmentDao);
        }

        private void loadEvent(ScheduledActivity parameterEvent) {
            this.scheduledActivity = load(parameterEvent, scheduledActivityDao);
        }

        ////// LOGIC

        public StudySite validateSiteInStudy() {
            for (StudySite studySite : this.getStudy().getStudySites()) {
                if (studySite.getSite().equals(this.getSite())) {
                    return studySite;
                }
            }
            throw new IllegalArgumentException("Site " + this.getSite().getGridId()
                    + " not associated with study " + this.getStudy().getGridId());
        }

        public void validateStudySegmentInStudy() {
            for (Epoch epoch : getStudy().getPlannedCalendar().getEpochs()) {
                if (epoch.getStudySegments().contains(getStudySegment())) {
                    return;
                }
            }
            throw new IllegalArgumentException("StudySegment " + getStudySegment().getGridId()
                    + " not part of template for study " + getStudy().getGridId());
        }

        public StudySubjectAssignment findAssignment() {
            return subjectDao.getAssignment(getSubject(), getStudy(), getSite());
        }

        ////// ACCESSORS

        public Study getStudy() {
            return study;
        }

        public Subject getSubject() {
            return subject;
        }

        public Site getSite() {
            return site;
        }

        public StudySegment getStudySegment() {
            if (studySegment == null) {
                return getStudy().getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
            } else {
                return studySegment;
            }
        }

        public ScheduledActivity getScheduledActivity() {
            return scheduledActivity;
        }
    }

}
