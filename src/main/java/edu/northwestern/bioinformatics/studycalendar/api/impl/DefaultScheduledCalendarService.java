package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

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
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledActivityDao scheduledActivityDao;
    private StudySubjectAssignmentDao studySubjectAssignmentDao;
    private UserDao userDao;

    public ScheduledCalendar assignSubject(
        Study study, Subject subject, Site site, Arm firstArm, Date startDate,String assignmentGridId
    ) {
        ParameterLoader loader = new ParameterLoader(study, site, firstArm);

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
                    "Use scheduleNextArm to change to the next arm.");
            }
        }

        StudySite join = loader.validateSiteInStudy();
        loader.validateArmInStudy();

        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        StudySubjectAssignment newAssignment = subjectService.assignSubject(
            loadedSubject, join, loader.getArm(), startDate, assignmentGridId, user);
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

    public void scheduleNextArm(
        Study study, Subject subject, Site site, Arm nextArm, NextArmMode mode, Date startDate
    ) {
        ParameterLoader loader = new ParameterLoader(study, subject, site, nextArm);
        subjectService.scheduleArm(loader.findAssignment(), loader.getArm(), startDate, mode);
    }

    public void registerSevereAdverseEvent(Study study, Subject subject, Site site, AdverseEvent adverseEvent) {
        ParameterLoader loader = new ParameterLoader(study, subject, site);
        StudySubjectAssignment assignment = loader.findAssignment();
        if (assignment == null) {
            throw new IllegalArgumentException("Subject is not assigned to this study at this site");
        }
        registerAeInternal(assignment, adverseEvent);
    }
    
    public void registerSevereAdverseEvent(StudySubjectAssignment assignment, AdverseEvent adverseEvent){
        StudySubjectAssignment loadedAssignment = load(assignment, studySubjectAssignmentDao);
        registerAeInternal(loadedAssignment, adverseEvent);
    }

    private void registerAeInternal(StudySubjectAssignment assignment, AdverseEvent adverseEvent) {
        AdverseEventNotification notification = new AdverseEventNotification();
        notification.setAdverseEvent(adverseEvent);
        assignment.addAeNotification(notification);

        subjectDao.save(assignment.getSubject());
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
    public void setArmDao(ArmDao armDao) {
        this.armDao = armDao;
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
    public void setStudySubjectAssignmentDao (StudySubjectAssignmentDao studySubjectAssignmentDao) {
        this.studySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
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
        private Arm arm;
        private ScheduledActivity scheduledActivity;

        public ParameterLoader(Study study, Subject subject, Site site) {
            loadStudy(study);
            loadSubject(subject);
            loadSite(site);
        }

        public ParameterLoader(Study study, Subject subject, Site site, Arm arm) {
            loadStudy(study);
            loadSubject(subject);
            loadSite(site);
            loadArm(arm);
        }

        public ParameterLoader(Study study, Site site, Arm arm) {
            loadStudy(study);
            loadSite(site);
            loadArm(arm);
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

        private void loadArm(Arm parameterArm) {
            this.arm = parameterArm == null ? null : load(parameterArm, armDao);
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

        public void validateArmInStudy() {
            for (Epoch epoch : getStudy().getPlannedCalendar().getEpochs()) {
                if (epoch.getArms().contains(getArm())) {
                    return;
                }
            }
            throw new IllegalArgumentException("Arm " + getArm().getGridId()
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

        public Arm getArm() {
            if (arm == null) {
                return getStudy().getPlannedCalendar().getEpochs().get(0).getArms().get(0);
            } else {
                return arm;
            }
        }

        public ScheduledActivity getScheduledActivity() {
            return scheduledActivity;
        }
    }

}
