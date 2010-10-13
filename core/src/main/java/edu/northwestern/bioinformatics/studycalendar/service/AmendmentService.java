package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.dao.PlannedActivityDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudySubjectAssignmentDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.*;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class AmendmentService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private StudyService studyService;
    private DeltaService deltaService;
    private TemplateService templateService;
    private PopulationService populationService;

    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private StudySubjectAssignmentDao StudySubjectAssignmentDao;
    private PlannedActivityDao plannedActivityDao;
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
        dev.setReleasedDate(new Date());
        deltaService.apply(source, dev);
        source.pushAmendment(dev);
        source.setDevelopmentAmendment(null);
        studyService.save(source);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void approve(StudySite studySite, AmendmentApproval... approvals) {
        for (AmendmentApproval approval : approvals) {
            List<String> emailAddressList = new ArrayList<String>();
            studySite.addAmendmentApproval(approval);
            Amendment amendment = approval.getAmendment();
            for (StudySubjectAssignment assignment : studySite.getStudySubjectAssignments()) {
                if (amendment.isMandatory()) {
                    // TODO: some sort of notification about applied vs. not-applied amendments
                    if (assignment.getCurrentAmendment().equals(amendment.getPreviousAmendment())) {
                        deltaService.amend(assignment, amendment);
                        Notification notification = new Notification(approval);
                        assignment.addNotification(notification);
                    } else {
                            log.info("Will not apply mandatory amendment {} to assignment {} as it has unapplied non-mandatory amendments intervening",
                                amendment.getDisplayName(), assignment.getId());
                    }
                } else {
                    Notification notification = Notification.createNotificationForNonMandatoryAmendments(assignment, amendment);
                    assignment.addNotification(notification);
                    StudySubjectAssignmentDao.save(assignment);
                }

                User studySubjectCalendarManager = assignment.getStudySubjectCalendarManager();
                if (studySubjectCalendarManager != null) {
                    if (!emailAddressList.contains(studySubjectCalendarManager.getEmailId())) {
                        emailAddressList.add(studySubjectCalendarManager.getEmailId());
                    }
                }
            }

            if (!emailAddressList.isEmpty()) {
                sendMailForNewAmendmentsInStudy(studySite.getStudy(), amendment, emailAddressList);
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
            deltaService.amendToPreviousVersion(amended);
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
    // TODO: this is sehr unDRY
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
    // TODO: this is sehr unDRY
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
     *
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
        Study study = templateService.findStudy(node);
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
     *
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

    /**
     * Removes a period
     *
     * @param period
     * @param studySegment
     */
    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void removePeriod(Period period, StudySegment studySegment) {
        for (PlannedActivity toRemove : new ArrayList<PlannedActivity>(period.getPlannedActivities())) {
            updateDevelopmentAmendment(period, Remove.create(toRemove));
        }
        updateDevelopmentAmendmentAndSave(studySegment, Remove.create(period));

    }

    public AmendmentApproval resolveAmentmentApproval(AmendmentApproval amendmentApproval, Study study) {
        Amendment amendment = amendmentDao.getByNaturalKey(amendmentApproval.getAmendment().getNaturalKey(), study);
        if (amendment == null && study != null) {
            throw new StudyCalendarValidationException("Amendment '%s' not found for study '%s'.",
                    amendmentApproval.getAmendment().getNaturalKey(), study.getAssignedIdentifier());
        }
        amendmentApproval.setAmendment(amendment);
        return amendmentApproval;
    }

    public void sendMailForNewAmendmentsInStudy(Study study, Amendment amendment, List<String> emailAddressList) {
        String subjectHeader = study.getAssignedIdentifier().concat(" has been amended");
        String message;
        if (amendment.isMandatory()) {
            message = "One or more subject schedules on ".concat(study.getAssignedIdentifier()).
                    concat(" have been amended according to ").concat(amendment.getName()).concat(" as of ").
                    concat(amendment.getDate().toString()).concat(". For more information, please login to Patient Study Calendar.");
        } else {
            message = "One or more subject schedules on ".concat(study.getAssignedIdentifier()).
                    concat(" may have been amended according to ").concat(amendment.getName()).concat(" as of ").
                    concat(amendment.getDate().toString()).concat(". For more information or to apply this amendment, please login to Patient Study Calendar.");
        }
        notificationService.sendNotificationMailToUsers(subjectHeader, message, emailAddressList);
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
    public void setPlannedActivityDao(PlannedActivityDao plannedActivityDao) {
        this.plannedActivityDao = plannedActivityDao;
    }

    @Required
    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
