package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Transactional
public class StudyService {
    private ActivityDao activityDao;
    private StudyDao studyDao;
    private DeltaService deltaService;

    public void scheduleReconsent(Study study, Date startDate, String details) throws Exception {
        List<StudyParticipantAssignment> participantAssignments = studyDao.getAssignmentsForStudy(study.getId());
        Activity reconsent = activityDao.getByName("Reconsent");
        for(StudyParticipantAssignment assignment: participantAssignments) {
            if (!assignment.isExpired()) {
                 ScheduledEvent nextScheduledEvent =  assignment.getScheduledCalendar().getNextScheduledEvent(startDate);
                if (nextScheduledEvent != null) {
                    ScheduledEvent event = new ScheduledEvent();
                    event.setIdealDate(nextScheduledEvent.getActualDate());
                    event.changeState(new Scheduled("Created From Reconsent", event.getIdealDate()));
                    event.setDetails(details);
                    event.setActivity(reconsent);
                    nextScheduledEvent.getScheduledArm().addEvent(event);
                }
            }
        }
        studyDao.save(study);
    }

    public void save(Study study) {
        deltaService.saveRevision(study.getAmendment());
        // TODO: use DeltaService#amend to merge in revision changes
        studyDao.save(study);
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
}
