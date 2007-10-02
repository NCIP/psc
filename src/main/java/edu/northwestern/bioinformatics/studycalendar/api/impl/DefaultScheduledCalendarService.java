package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.dao.*;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;
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
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;
    private ScheduledEventDao scheduledEventDao;
    private StudyParticipantAssignmentDao studyParticipantAssignmentDao;
    private UserDao userDao;

    public ScheduledCalendar assignParticipant(
        Study study, Participant participant, Site site, Arm firstArm, Date startDate,String assignmentGridId
    ) {
        ParameterLoader loader = new ParameterLoader(study, site, firstArm);

        Participant loadedParticipant = load(participant, participantDao, false);
        if (loadedParticipant == null) {
            participantDao.save(participant);
            loadedParticipant = participant;
        } else {
            StudyParticipantAssignment assignment = participantDao.getAssignment(
                loadedParticipant, loader.getStudy(), loader.getSite());
            if (assignment != null) {
                throw new IllegalArgumentException(
                    "Participant already assigned to this study.  " +
                    "Use scheduleNextArm to change to the next arm.");
            }
        }

        StudySite join = loader.validateSiteInStudy();
        loader.validateArmInStudy();

        String userName = ApplicationSecurityManager.getUser();
        User user = userDao.getByName(userName);
        StudyParticipantAssignment newAssignment = participantService.assignParticipant(
            loadedParticipant, join, loader.getArm(), startDate, assignmentGridId, user);
        return newAssignment.getScheduledCalendar();
    }

    public ScheduledCalendar getScheduledCalendar(Study study, Participant participant, Site site) {
        ParameterLoader loader = new ParameterLoader(study, participant, site);

        StudyParticipantAssignment assignment = loader.findAssignment();
        if (assignment == null) {
            return null;
        } else {
            ScheduledCalendar calendar = assignment.getScheduledCalendar();
            scheduledCalendarDao.initialize(calendar);
            return calendar;
        }
    }

    public Collection<ScheduledEvent> getScheduledEvents(
        Study study, Participant participant, Site site, Date startDate, Date endDate
    ) {
        ParameterLoader loader = new ParameterLoader(study, participant, site);
        ScheduledCalendar calendar = loader.findAssignment().getScheduledCalendar();
        return scheduledEventDao.getEventsByDate(calendar, startDate, endDate);
    }

    public ScheduledEvent changeEventState(ScheduledEvent event, ScheduledEventState newState) {
        ParameterLoader loader = new ParameterLoader(event);

        loader.getScheduledEvent().changeState(newState);
        scheduledEventDao.save(loader.getScheduledEvent());
        return loader.getScheduledEvent();
    }

    public void scheduleNextArm(
        Study study, Participant participant, Site site, Arm nextArm, NextArmMode mode, Date startDate
    ) {
        ParameterLoader loader = new ParameterLoader(study, participant, site, nextArm);
        participantService.scheduleArm(loader.findAssignment(), loader.getArm(), startDate, mode);
    }

    public void registerSevereAdverseEvent(Study study, Participant participant, Site site, AdverseEvent adverseEvent) {
        ParameterLoader loader = new ParameterLoader(study, participant, site);
        StudyParticipantAssignment assignment = loader.findAssignment();
        if (assignment == null) {
            throw new IllegalArgumentException("Participant is not assigned to this study at this site");
        }
        registerAeInternal(assignment, adverseEvent);
    }
    
    public void registerSevereAdverseEvent(StudyParticipantAssignment assignment, AdverseEvent adverseEvent){
        StudyParticipantAssignment loadedAssignment = load(assignment, studyParticipantAssignmentDao);
        registerAeInternal(loadedAssignment, adverseEvent);
    }

    private void registerAeInternal(StudyParticipantAssignment assignment, AdverseEvent adverseEvent) {
        AdverseEventNotification notification = new AdverseEventNotification();
        notification.setAdverseEvent(adverseEvent);
        assignment.addAeNotification(notification);

        participantDao.save(assignment.getParticipant());
    }

    ////// CONFIGURATION

    @Required
    public void setParticipantDao(ParticipantDao participantDao) {
        this.participantDao = participantDao;
    }

    @Required
    public void setParticipantService(ParticipantService participantService) {
        this.participantService = participantService;
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
    public void setScheduledEventDao(ScheduledEventDao scheduledEventDao) {
        this.scheduledEventDao = scheduledEventDao;
    }
    
    @Required
    public void setStudyParticipantAssignmentDao(StudyParticipantAssignmentDao studyParticipantAssignmentDao) {
        this.studyParticipantAssignmentDao = studyParticipantAssignmentDao;
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
        private Participant participant;
        private Site site;
        private Arm arm;
        private ScheduledEvent scheduledEvent;

        public ParameterLoader(Study study, Participant participant, Site site) {
            loadStudy(study);
            loadParticipant(participant);
            loadSite(site);
        }

        public ParameterLoader(Study study, Participant participant, Site site, Arm arm) {
            loadStudy(study);
            loadParticipant(participant);
            loadSite(site);
            loadArm(arm);
        }

        public ParameterLoader(Study study, Site site, Arm arm) {
            loadStudy(study);
            loadSite(site);
            loadArm(arm);
        }

        public ParameterLoader(ScheduledEvent scheduledEvent) {
            loadEvent(scheduledEvent);
        }

        ////// LOADING

        private void loadStudy(Study parameterStudy) {
            this.study = load(parameterStudy, studyDao);
        }

        private void loadSite(Site parameterSite) {
            this.site = load(parameterSite, siteDao);
        }

        private void loadParticipant(Participant parameterParticipant) {
            this.participant = load(parameterParticipant, participantDao);
        }

        private void loadArm(Arm parameterArm) {
            this.arm = parameterArm == null ? null : load(parameterArm, armDao);
        }

        private void loadEvent(ScheduledEvent parameterEvent) {
            this.scheduledEvent = load(parameterEvent, scheduledEventDao);
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

        public StudyParticipantAssignment findAssignment() {
            return participantDao.getAssignment(getParticipant(), getStudy(), getSite());
        }

        ////// ACCESSORS

        public Study getStudy() {
            return study;
        }

        public Participant getParticipant() {
            return participant;
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

        public ScheduledEvent getScheduledEvent() {
            return scheduledEvent;
        }
    }

}
