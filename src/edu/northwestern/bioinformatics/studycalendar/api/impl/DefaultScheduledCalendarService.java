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
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import edu.northwestern.bioinformatics.studycalendar.dao.ParticipantDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
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

    public ScheduledCalendar assignParticipant(Study study, Participant participant, Site site, Arm firstArm) {
        throw new UnsupportedOperationException("assignParticipant not implemented");
    }

    public ScheduledCalendar getScheduledCalendar(Study study, Participant participant, Site site) {
        checkForBigId(study);
        checkForBigId(participant);
        checkForBigId(site);

        Study loadedStudy = studyDao.getByBigId(study);
        Participant loadedParticipant = participantDao.getByBigId(participant);
        Site loadedSite = siteDao.getByBigId(site);

        if (loadedStudy == null) throw createGetScheduleParameterException(study);
        if (loadedParticipant == null) throw createGetScheduleParameterException(participant);
        if (loadedSite == null) throw createGetScheduleParameterException(site);

        StudyParticipantAssignment assignment = participantDao.getAssignment(loadedParticipant, loadedStudy, loadedSite);
        return assignment == null ? null : assignment.getScheduledCalendar();
    }

    private void checkForBigId(AbstractDomainObjectWithBigId withBigId) {
        if (!withBigId.hasBigId()) {
            throw new IllegalArgumentException(
                "Could not get schedule: no bigId on "
                    + withBigId.getClass().getSimpleName().toLowerCase() + " parameter");
        }
    }

    private IllegalArgumentException createGetScheduleParameterException(WithBigId withBigId) {
        return new IllegalArgumentException(
            "Could not get schedule: no " + withBigId.getClass().getSimpleName().toLowerCase() +
                " with bigId " + withBigId.getBigId());
    }

    public Collection<ScheduledEvent> getScheduledEvents(Study study, Participant participant, Site site, Date startDate, Date endDate) {
        throw new UnsupportedOperationException("getScheduledEvents not implemented");
    }

    public ScheduledEvent changeEventState(ScheduledEvent event, ScheduledEventState newState) {
        throw new UnsupportedOperationException("changeEventState not implemented");
    }

    public void scheduleNextArm(Study study, Participant participant, Site site, Arm nextArm, NextArmMode mode, Date startDate) {
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

    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
