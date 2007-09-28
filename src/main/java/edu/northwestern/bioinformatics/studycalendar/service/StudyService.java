package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
public class StudyService {
    private ActivityDao activityDao;
    private StudyDao studyDao;
    private DeltaService deltaService;
    private TemplateService templateService;

    public void scheduleReconsent(Study study, Date startDate, String details) throws Exception {
        List<StudyParticipantAssignment> participantAssignments = studyDao.getAssignmentsForStudy(study.getId());
        Activity reconsent = activityDao.getByName("Reconsent");
        for(StudyParticipantAssignment assignment: participantAssignments) {
            if (!assignment.isExpired()) {
                ScheduledEvent upcomingScheduledEvent = getNextScheduledEvent(assignment.getScheduledCalendar(), startDate);
                if (upcomingScheduledEvent != null) {
                    ScheduledEvent reconsentEvent = new ScheduledEvent();
                    reconsentEvent.setIdealDate(upcomingScheduledEvent.getActualDate());
                    reconsentEvent.changeState(new Scheduled("Created From Reconsent", upcomingScheduledEvent.getActualDate()));
                    reconsentEvent.setDetails(details);
                    reconsentEvent.setActivity(reconsent);
                    upcomingScheduledEvent.getScheduledArm().addEvent(reconsentEvent);
                }
            }
        }
        studyDao.save(study);
    }

    private ScheduledEvent getNextScheduledEvent(ScheduledCalendar calendar, Date startDate) {
        for (ScheduledArm arm : calendar.getScheduledArms()) {
            if (!arm.isComplete()) {
                Map<Date, List<ScheduledEvent>> eventsByDate = arm.getEventsByDate();
                for(Date date: eventsByDate.keySet()) {
                    List<ScheduledEvent> events = eventsByDate.get(date);
                    for(ScheduledEvent event : events) {
                        if ((event.getActualDate().after(startDate) || event.getActualDate().equals(startDate))
                                && ScheduledEventMode.SCHEDULED == event.getCurrentState().getMode() ) {
                            return event;
                        }
                    }
                }
            }
        }
        return null;
    }

    // TODO: need replace all business uses of StudyDao#save with this method
    public void save(Study study) {
        studyDao.save(study);
        if (study.getAmendment() != null) {
            deltaService.saveRevision(study.getAmendment());
        }
        if (study.getDevelopmentAmendment() != null) {
            deltaService.saveRevision(study.getDevelopmentAmendment());
        }
    }

    public Study saveStudyFor(PlanTreeNode<?> node) {
        Study study = templateService.findStudy(node);
        save(study);
        return study;
    }

    ////// CONFIGURATION

    @Required
    public void setActivityDao(ActivityDao activityDao) {
        this.activityDao = activityDao;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }
}
