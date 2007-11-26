package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
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
        List<StudySubjectAssignment> subjectAssignments = studyDao.getAssignmentsForStudy(study.getId());
        Activity reconsent = activityDao.getByName("Reconsent");
        for(StudySubjectAssignment assignment: subjectAssignments) {
            if (!assignment.isExpired()) {
                ScheduledActivity upcomingScheduledActivity = getNextScheduledActivity(assignment.getScheduledCalendar(), startDate);
                if (upcomingScheduledActivity != null) {
                    ScheduledActivity reconsentEvent = new ScheduledActivity();
                    reconsentEvent.setIdealDate(upcomingScheduledActivity.getActualDate());
                    reconsentEvent.changeState(new Scheduled("Created From Reconsent", upcomingScheduledActivity.getActualDate()));
                    reconsentEvent.setDetails(details);
                    reconsentEvent.setActivity(reconsent);
                    reconsentEvent.setSourceAmendment(study.getAmendment());
                    upcomingScheduledActivity.getScheduledStudySegment().addEvent(reconsentEvent);
                }
            }
        }
        studyDao.save(study);
    }

    private ScheduledActivity getNextScheduledActivity(ScheduledCalendar calendar, Date startDate) {
        for (ScheduledStudySegment studySegment : calendar.getScheduledStudySegments()) {
            if (!studySegment.isComplete()) {
                Map<Date, List<ScheduledActivity>> eventsByDate = studySegment.getEventsByDate();
                for(Date date: eventsByDate.keySet()) {
                    List<ScheduledActivity> events = eventsByDate.get(date);
                    for(ScheduledActivity event : events) {
                        if ((event.getActualDate().after(startDate) || event.getActualDate().equals(startDate))
                                && ScheduledActivityMode.SCHEDULED == event.getCurrentState().getMode() ) {
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
