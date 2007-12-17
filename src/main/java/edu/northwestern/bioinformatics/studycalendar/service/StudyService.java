package edu.northwestern.bioinformatics.studycalendar.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;

@Transactional
public class StudyService {
	private ActivityDao activityDao;

	private StudyDao studyDao;

	private DeltaService deltaService;

	private TemplateService templateService;

	private PlannedCalendarDao plannedCalendarDao;

	public void scheduleReconsent(final Study study, final Date startDate, final String details) throws Exception {
		List<StudySubjectAssignment> subjectAssignments = studyDao.getAssignmentsForStudy(study.getId());
		Activity reconsent = activityDao.getByName("Reconsent");
		for (StudySubjectAssignment assignment : subjectAssignments) {
			if (!assignment.isExpired()) {
				ScheduledActivity upcomingScheduledActivity = getNextScheduledActivity(assignment
						.getScheduledCalendar(), startDate);
				if (upcomingScheduledActivity != null) {
					ScheduledActivity reconsentEvent = new ScheduledActivity();
					reconsentEvent.setIdealDate(upcomingScheduledActivity.getActualDate());
					reconsentEvent.changeState(new Scheduled("Created From Reconsent", upcomingScheduledActivity
							.getActualDate()));
					reconsentEvent.setDetails(details);
					reconsentEvent.setActivity(reconsent);
					reconsentEvent.setSourceAmendment(study.getAmendment());
					upcomingScheduledActivity.getScheduledStudySegment().addEvent(reconsentEvent);
				}
			}
		}
		studyDao.save(study);
	}

	private ScheduledActivity getNextScheduledActivity(final ScheduledCalendar calendar, final Date startDate) {
		for (ScheduledStudySegment studySegment : calendar.getScheduledStudySegments()) {
			if (!studySegment.isComplete()) {
				Map<Date, List<ScheduledActivity>> eventsByDate = studySegment.getActivitiesByDate();
				for (Date date : eventsByDate.keySet()) {
					List<ScheduledActivity> events = eventsByDate.get(date);
					for (ScheduledActivity event : events) {
						if ((event.getActualDate().after(startDate) || event.getActualDate().equals(startDate))
								&& ScheduledActivityMode.SCHEDULED == event.getCurrentState().getMode()) {
							return event;
						}
					}
				}
			}
		}
		return null;
	}

	// TODO: need replace all business uses of StudyDao#save with this method
	public void save(final Study study) {
		studyDao.save(study);
		if (study.getAmendment() != null) {
			deltaService.saveRevision(study.getAmendment());
		}
		if (study.getDevelopmentAmendment() != null) {
			deltaService.saveRevision(study.getDevelopmentAmendment());
		}
	}

	public Study saveStudyFor(final PlanTreeNode<?> node) {
		Study study = templateService.findStudy(node);
		save(study);
		return study;
	}

    public Study getStudyByAssignedIdentifier(final String assignedIdentifier) {

        Study study = studyDao.getStudyByAssignedIdentifier(assignedIdentifier);
        if (study == null) {
            return null;
        }
        Hibernate.initialize(study);
        List<StudySite> sites = study.getStudySites();
        Hibernate.initialize(sites);
        for (StudySite studySite : sites) {
            Hibernate.initialize(studySite);
            Hibernate.initialize(studySite.getCurrentApprovedAmendment());
        }

        plannedCalendarDao.initialize(study.getPlannedCalendar());
        return study;
    }

	// //// CONFIGURATION

	@Required
	public void setActivityDao(final ActivityDao activityDao) {
		this.activityDao = activityDao;
	}

	@Required
	public void setStudyDao(final StudyDao studyDao) {
		this.studyDao = studyDao;
	}

	@Required
	public void setDeltaService(final DeltaService deltaService) {
		this.deltaService = deltaService;
	}

	@Required
	public void setTemplateService(final TemplateService templateService) {
		this.templateService = templateService;
	}

	@Required
	public void setPlannedCalendarDao(final PlannedCalendarDao plannedCalendarDao) {
		this.plannedCalendarDao = plannedCalendarDao;
	}
}
