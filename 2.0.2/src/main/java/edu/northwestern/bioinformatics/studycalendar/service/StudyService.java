package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.dao.ActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedCalendarDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Activity;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledStudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.PlannedCalendarDelta;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.Scheduled;
import gov.nih.nci.cabig.ctms.lang.NowFactory;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Transactional
public class StudyService {
	private ActivityDao activityDao;
	private StudyDao studyDao;
	private DeltaService deltaService;
	private TemplateService templateService;
	private PlannedCalendarDao plannedCalendarDao;
    private AmendmentDao  amendmentDao;
    private EpochDao epochDao;
    private NowFactory nowFactory;

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

    public String getNewStudyName() {
        return studyDao.getNewStudyName();
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
            for (Amendment amendment : study.getAmendmentsList()) {
                deltaService.saveRevision(amendment);
            }
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

        PlannedCalendar plannedCalendar = study.getPlannedCalendar();
        if (plannedCalendar != null) {
            plannedCalendarDao.initialize(plannedCalendar);

        }

        List<Amendment> amendmentList = study.getAmendmentsList();
        for (Amendment amendment : amendmentList) {
            initializeAmendment(amendment);

        }

        Amendment developmentAmendment = study.getDevelopmentAmendment();
        initializeAmendment(developmentAmendment);
        return study;
    }

    private void initializeAmendment(Amendment amendment) {
        Hibernate.initialize(amendment);
        if (amendment != null) {
            List<Delta<?>> deltas = amendment.getDeltas();
            Hibernate.initialize(deltas);
            for (Delta delta : deltas) {
                Hibernate.initialize(delta.getChanges());
                List<Change> changes = delta.getChanges();
                for (Change change : changes) {
                    Hibernate.initialize(change);
                }
            }
        }
    }

    /**
     * Mutates the provided example study into a saveable form and then saves it.
     * Specifically, it takes the plan tree embodied in the plannedCalendar of the
     * example study and translates it into a development amendment which, when
     * released, will have the same structure as the example.
     *
     * @param example
     */
    public void createInDesignStudyFromExamplePlanTree(Study example) {
        example.setAmendment(null);
        Amendment newDev = new Amendment();
        newDev.setDate(nowFactory.getNow());
        newDev.setName(Amendment.INITIAL_TEMPLATE_AMENDMENT_NAME);

        PlannedCalendarDelta delta = new PlannedCalendarDelta(example.getPlannedCalendar());
        List<Epoch> epochs = new ArrayList<Epoch>(example.getPlannedCalendar().getEpochs());
        example.getPlannedCalendar().getEpochs().clear();
        for (Epoch epoch : epochs) {
            Add.create(epoch).mergeInto(delta);
        }
        newDev.addDelta(delta);

        example.setDevelopmentAmendment(newDev);
        save(example);
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

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao;
    }

    @Required
    public void setNowFactory(NowFactory nowFactory) {
        this.nowFactory = nowFactory;
    }
}
