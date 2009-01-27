package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Rhett Sutphin
 */
public class AmendmentService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyService studyService;
    private DeltaService deltaService;
    private TemplateService templateService;
    private PopulationService populationService;
    private NotificationService notificationService;

    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudySubjectAssignmentDao StudySubjectAssignmentDao;
    private PlannedActivityDao plannedActivityDao;

    /**
     * Commit the changes in the developmentAmendment for the given study.  This means:
     * <ul>
     * <li>Apply the deltas to the persistent calendar</li>
     * <li>Move the development amendment to the study's amendment stack</li>
     * <li>Save it all</li>
     * </ul>
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void amend(Study source) {
        Amendment dev = source.getDevelopmentAmendment();
        if (dev == null) {
            throw new StudyCalendarSystemException("%s has no development amendment", source);
        }
        dev.setReleasedDate(new Date());
        deltaService.apply(source, dev);
        source.pushAmendment(dev);
        source.setDevelopmentAmendment(null);
        studyService.save(source);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void approve(StudySite studySite, AmendmentApproval... approvals) {
        for (AmendmentApproval approval : approvals) {
            studySite.addAmendmentApproval(approval);
            if (approval.getAmendment().isMandatory()) {
                for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
                    // TODO: some sort of notification about applied vs. not-applied amendments
                    if (assignment.getCurrentAmendment().equals(approval.getAmendment().getPreviousAmendment())) {
                        deltaService.amend(assignment, approval.getAmendment());
                        Notification notification = new Notification(approval);
                        assignment.addNotification(notification);
                        notificationService.notifyUsersForNewScheduleNotifications(notification);


                    } else {
                        log.info("Will not apply mandatory amendment {} to assignment {} as it has unapplied non-mandatory amendments intervening",
                                approval.getAmendment().getDisplayName(), assignment.getId());
                    }
                }
            } else {
                for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
                    Notification notification = Notification.createNotificationForNonMandatoryAmendments(assignment, approval.getAmendment());
                    assignment.addNotification(notification);
                    notificationService.notifyUsersForNewScheduleNotifications(notification);
                    StudySubjectAssignmentDao.save(assignment);

                }
            }
        }
    }

    /**
     * Takes the provided source study and rolls it back to the amendment
     */
    public Study getAmendedStudy(Study source, Amendment target) {
        if (!(source.getAmendment().equals(target) || source.getAmendment().hasPreviousAmendment(target))) {
            throw new StudyCalendarSystemException(
                    "Amendment %s (%s) does not apply to the template for %s (%s)",
                    target.getName(), target.getGridId(), source.getName(), source.getGridId());
        }

        Study amended = source.transientClone();
        while (!target.equals(amended.getAmendment())) {
            log.debug("Rolling {} back to {}", source, amended.getAmendment().getPreviousAmendment().getName());
            deltaService.revert(amended, amended.getAmendment());
            amended.setAmendment(amended.getAmendment().getPreviousAmendment());
        }

        return amended;
    }

    @SuppressWarnings({"unchecked"})
    public <T extends PlanTreeNode<?>> T getAmendedNode(T source, Amendment target) {
        Study base = templateService.findStudy(source);
        Study amended = getAmendedStudy(base, target);
        return (T) templateService.findEquivalentChild(amended, source);
    }



    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Study updateDevelopmentAmendmentForStudyAndSave(Study study, Change... changes) {
        log.debug("Updating dev amendment for study {} with {} change(s)", study, changes.length);
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException("The study %s is not open for editing or amending", study);
        }
        for (Change change : changes) {
            deltaService.updateRevisionForStudy(study.getDevelopmentAmendment(), study, change);
        }
        studyService.save(study);
        return study;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Population updateDevelopmentAmendmentForStudyAndSave(Population population, Study study, Change... changes) {
        log.debug("Updating dev amendment for study {} with {} change(s)", population, changes.length);
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException("The study %s is not open for editing or amending", population.getStudy());
        }
        for (Change change : changes) {
            deltaService.updateRevision(study.getDevelopmentAmendment(), population, change);
        }
        studyService.save(study);
        return population;
    }

    /**
     * Finds the current development amendment for the study associated with the node
     * and merges in the given change.
     * @return the study that the
     */
    public void updateDevelopmentAmendment(PlanTreeNode<?> node, Change... changes) {
        updateDevelopmentAmendmentInternal(node, changes);
    }

    // internal helper to avoid changing the widely-used signature of updateDevelopmentAmendment
    // immediately before 2.2.  TODO: merge back with updateDevelopmentAmendment later.
    private Study updateDevelopmentAmendmentInternal(PlanTreeNode<?> node, Change... changes) {
        log.debug("Updating dev amendment for node {} with {} change(s)", node, changes.length);
        node = templateService.findCurrentNode(node);
        log.debug("Current persistent node is {}", node);
        Study study = templateService.findAncestor(node, PlannedCalendar.class).getStudy();
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException("The study %s is not open for editing or amending", study);
        }
        for (Change change : changes) {
            deltaService.updateRevision(study.getDevelopmentAmendment(), node, change);
        }
        return study;
    }

    /**
     * Applies a series of changes to the development amendment for the study associated
     * with the node and then saves the associated study.  Unlike #updateDevelopmentAmendment,
     * this method occurs in a single transaction.
     * @see StudyService#saveStudyFor
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public Study updateDevelopmentAmendmentAndSave(PlanTreeNode<?> node, Change... changes) {
        Study study = updateDevelopmentAmendmentInternal(node, changes);
        studyService.save(study);
        return study;
    }

    /**
     * Special case service method which records a planned activity for addition to a period.
     * Differs from the general {@link #updateDevelopmentAmendmentAndSave} in that it makes
     * a special effort to prevent optimistic locking failures by <strong>not updating
     * the version of the container to which the planned activity is added</strong>.  This means
     * it is only safe to use in certain circumstances.  Beware.
     */
    public Study addPlannedActivityToDevelopmentAmendmentAndSave(Period node, PlannedActivity plannedActivity) {
        node = templateService.findCurrentNode(node);
        if (node.isDetached()) {
            log.debug("Detached period; save planned activity only");
            // deliberately not setting the Period => PA reference
            plannedActivity.setPeriod(node);
            plannedActivityDao.save(plannedActivity);
            return templateService.findStudy(node);
        } else {
            log.debug("Attached period; create new Add");
            return updateDevelopmentAmendmentAndSave(node, Add.create(plannedActivity));
        }
    }


    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }

    @Required
    public void setTemplateService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    @Required
    public void setPopulationService(final PopulationService populationService) {
        this.populationService = populationService;
    }

    @Required
    public void setStudySubjectAssignmentDao(final StudySubjectAssignmentDao studySubjectAssignmentDao) {
        StudySubjectAssignmentDao = studySubjectAssignmentDao;
    }

    @Required
    public void setNotificationService(final NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Required
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }
}
