package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Rhett Sutphin
 */
public class AmendmentService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyService studyService;
    private DeltaService deltaService;
    private TemplateService templateService;
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private PopulationService populationService;
    private StudySubjectAssignmentDao StudySubjectAssignmentDao;
    private NotificationService notificationService;


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

    /**
     * Finds the current development amendment for the study associated with the node
     * and merges in the given change.
     */
    public void updateDevelopmentAmendment(PlanTreeNode<?> node, Change change) {
        Study study = templateService.findAncestor(node, PlannedCalendar.class).getStudy();
        if (!study.isInDevelopment()) {
            throw new StudyCalendarSystemException("The study %s is not open for editing or amending", study);
        }
        deltaService.updateRevision(study.getDevelopmentAmendment(), node, change);
    }

    /**
     * Deletes the development amendment for the designated study.  If the
     * study has no released amendment, it deletes the study and the study's
     * planned calendar.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteDevelopmentAmendment(Study study) {
        deleteDevelopmentAmendment(study.getDevelopmentAmendment());
        populationService.delete(study.getPopulations());
        if (study.getAmendment() == null) {
            templateService.delete(study.getPlannedCalendar());
            studyDao.delete(study);
        } else {
            study.setDevelopmentAmendment(null);
            studyService.save(study);
        }
    }

    /**
     * Deletes the development amendment for the designated study.  Even if the
     * study has no released amendment, it does not delete the study and the study's
     * planned calendar.
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteDevelopmentAmendmentOnly(Study study) {
        deleteDevelopmentAmendment(study.getDevelopmentAmendment());
        study.setDevelopmentAmendment(null);
        studyService.save(study);
    }

    private void deleteDevelopmentAmendment(Amendment dev) {
        if (dev != null) {
            for (Delta<?> delta : dev.getDeltas()) {
                deltaService.delete(delta);
            }
            amendmentDao.delete(dev);
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
}
