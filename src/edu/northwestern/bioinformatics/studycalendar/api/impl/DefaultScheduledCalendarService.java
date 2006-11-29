package edu.northwestern.bioinformatics.studycalendar.api.impl;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.NextArmMode;
import edu.northwestern.bioinformatics.studycalendar.domain.WithBigId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObjectWithBigId;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ArmDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ScheduledCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.WithBigIdDao;
import edu.northwestern.bioinformatics.studycalendar.service.ParticipantService;

import java.util.Collection;
import java.util.Date;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class DefaultScheduledCalendarService implements ScheduledCalendarService {
    private ParticipantDao participantDao;
    private ParticipantService participantService;
    private StudyDao studyDao;
    private SiteDao siteDao;
    private ArmDao armDao;
    private ScheduledCalendarDao scheduledCalendarDao;

    public ScheduledCalendar assignParticipant(
        Study study, Participant participant, Site site, Arm firstArm, Date startDate
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

        StudyParticipantAssignment newAssignment = participantService.assignParticipant(
            loadedParticipant, join, loader.getArm(), startDate);
        return newAssignment.getScheduledCalendar();
    }

    public ScheduledCalendar getScheduledCalendar(Study study, Participant participant, Site site) {
        ParameterLoader loader = new ParameterLoader(study, participant, site);

        StudyParticipantAssignment assignment = participantDao.getAssignment(
            loader.getParticipant(), loader.getStudy(), loader.getSite());
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
        throw new UnsupportedOperationException("getScheduledEvents not implemented");
    }

    public ScheduledEvent changeEventState(ScheduledEvent event, ScheduledEventState newState) {
        throw new UnsupportedOperationException("changeEventState not implemented");
    }

    public void scheduleNextArm(
        Study study, Participant participant, Site site, Arm nextArm, NextArmMode mode, Date startDate
    ) {
        throw new UnsupportedOperationException("scheduleNextArm not implemented");
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

    //////

    private <T extends AbstractDomainObjectWithBigId> T load(T parameter, WithBigIdDao<T> dao) {
        return load(parameter, dao, true);
    }

    private <T extends AbstractDomainObjectWithBigId> T load(T parameter, WithBigIdDao<T> dao, boolean required) {
        checkForBigId(parameter);
        T loaded = dao.getByBigId(parameter);
        if (required && loaded == null) {
            throw new IllegalArgumentException("No " + parameter.getClass().getSimpleName().toLowerCase() +
                " with bigId " + parameter.getBigId());
        }
        return loaded;
    }

    private void checkForBigId(AbstractDomainObjectWithBigId withBigId) {
        if (!withBigId.hasBigId()) {
            throw new IllegalArgumentException(
                "No bigId on " + withBigId.getClass().getSimpleName().toLowerCase() + " parameter");
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

        public ParameterLoader(Study study, Site site, Arm arm) {
            loadStudy(study);
            loadSite(site);
            loadArm(arm);
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
            this.arm = load(parameterArm, armDao);
        }

        ////// LOGIC

        private StudySite validateSiteInStudy() {
            for (StudySite studySite : this.getStudy().getStudySites()) {
                if (studySite.getSite().equals(this.getSite())) {
                    return studySite;
                }
            }
            throw new IllegalArgumentException("Site " + this.getSite().getBigId()
                + " not associated with study " + this.getStudy().getBigId());
        }

        private void validateArmInStudy() {
            for (Epoch epoch : getStudy().getPlannedCalendar().getEpochs()) {
                if (epoch.getArms().contains(getArm())) {
                    return;
                }
            }
            throw new IllegalArgumentException("Arm " + getArm().getBigId()
                + " not part of template for study " + getStudy().getBigId());
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
            return arm;
        }

        public ScheduledEvent getScheduledEvent() {
            return scheduledEvent;
        }
    }
}
