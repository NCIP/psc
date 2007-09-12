package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public abstract class EditCommand {
    private DeltaService deltaService;
    private StudyService studyService;

    // directly bound
    private Study study;
    private Epoch epoch;
    private Arm arm;

    // revised
    private Study revisedStudy;
    private Epoch revisedEpoch;
    private Arm revisedArm;

    public void apply() {
        Study target = getStudy();
        verifyEditable(target);
        performEdit();
        studyService.save(target);
        cleanUpdateRevised();
    }

    private void verifyEditable(Study target) {
        if (!target.isInDevelopment()) {
            throw new StudyCalendarSystemException(
                "The study %s is not in development and so may not be edited.", target.getName());
        }
    }

    /**
     * Template method for providing objects to the view
     */
    public Map<String, Object> getModel() {
        return new HashMap<String, Object>();
    }

    /**
     * Template method that performs the actual work of the command
     */
    protected abstract void performEdit();

    protected abstract String getRelativeViewName();

    /* TODO: probably don't need this anymore
    protected Study toSave() {
        if (getStudy() != null) {
            return getStudy();
        } else if (getEpoch() != null) {
            return getEpoch().getPlannedCalendar().getStudy();
        } else if (getArm() != null) {
            return getArm().getEpoch().getPlannedCalendar().getStudy();
        } else {
            throw new IllegalStateException("Cannot determine which study the edit was applied to");
        }
    }*/

    protected void updateRevision(PlanTreeNode<?> node, Change change) {
        deltaService.updateRevision(getStudy().getDevelopmentAmendment(), node, change);
        cleanUpdateRevised();
    }

    private void cleanUpdateRevised() {
        revisedStudy = null; // reset
        updateRevised();
    }

    private void updateRevised() {
        if (getStudy() != null && revisedStudy == null) {
            revisedStudy = deltaService.revise(getStudy(), getStudy().getDevelopmentAmendment());
        }
        if (revisedStudy != null  && (getEpoch() != null || getArm() != null)) {
            for (Epoch e : revisedStudy.getPlannedCalendar().getEpochs()) {
                if (getEpoch() != null && e.getId().equals(getEpoch().getId())) {
                    revisedEpoch = e;
                }
                for (Arm a : e.getArms()) {
                    if (getArm() != null && a.getId().equals(getArm().getId())) {
                        revisedArm = a;
                    }
                }
            }
        }
    }

    public Study getRevisedStudy() {
        return revisedStudy;
    }

    public Epoch getRevisedEpoch() {
        return revisedEpoch;
    }

    public Arm getRevisedArm() {
        return revisedArm;
    }

    public PlannedCalendar getSafeEpochParent() {
        return getSafeParent(getEpoch(), getRevisedEpoch());
    }

    public Epoch getSafeArmParent() {
        return getSafeParent(getArm(), getRevisedArm());
    }

    private <P extends DomainObject> P getSafeParent(PlanTreeNode<P> bound, PlanTreeNode<P> revised) {
        // these casts are safe because this method is only used with Arms or Epochs
        if (bound.getParent() == null) {
            // If the thing targeted is newly added, its parent will be null
            // In order to update the parent's delta, we need to find the parent in the revised tree
            return revised.getParent();
        } else {
            // However, if it isn't newly added, it might not have any other changes
            // in order to create the delta properly, we need to use the persistent one
            return bound.getParent();
        }
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        verifyEditable(study);
        this.study = study;
        updateRevised();
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
        updateRevised();
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
        updateRevised();
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setDeltaService(DeltaService deltaService) {
        this.deltaService = deltaService;
    }
}
