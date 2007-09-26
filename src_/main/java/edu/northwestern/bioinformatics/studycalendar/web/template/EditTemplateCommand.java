package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlanTreeNode;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Required;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * Base class for commands invoked from the main display template page.
 *
 * @author Rhett Sutphin
 */
public abstract class EditTemplateCommand implements EditCommand<PlannedCalendar> {
    private Mode mode;
    private DeltaService deltaService;
    private StudyService studyService;
    private DaoFinder daoFinder;

    // directly bound
    private Study study;
    private Epoch epoch;
    private Arm arm;

    // revised
    private Study revisedStudy;
    private Epoch revisedEpoch;
    private Arm revisedArm;

    public PlannedCalendar apply() {
        Study target = getStudy();
        verifyEditable(target);
        performEdit();
        studyService.save(target);
        cleanUpdateRevised();
        return null;
    }

    public void performEdit() {
        getMode().performEdit();
    }

    private void verifyEditable(Study target) {
        if (!target.isInDevelopment()) {
            throw new StudyCalendarSystemException(
                "The study %s is not in development and so may not be edited.", target.getName());
        }
    }

    public Map<String, Object> getModel() {
        Map<String, Object> model = new HashMap<String, Object>();
        Map<String, Object> modeModel = getMode().getModel();
        model.put("developmentRevision", getStudy().getDevelopmentAmendment());
        model.put("revisionChanges",
            new RevisionChanges(daoFinder, getStudy().getDevelopmentAmendment(), getStudy()));
        if (modeModel != null) {
            model.putAll(modeModel);
        }
        return model;
    }

    protected void updateRevision(PlanTreeNode<?> node, Change change) {
        deltaService.updateRevision(getStudy().getDevelopmentAmendment(),node, change);
        cleanUpdateRevised();
    }

    ////// MODES
    // Subclasses should provide a mode for handling each type of bound domain object
    // that makes sense

    public String getRelativeViewName() {
        return getMode().getRelativeViewName();
    }

    private Mode getMode() {
        if (mode == null) mode = selectMode();
        return mode;
    }

    protected Mode studyMode() { throw new UnsupportedOperationException("No study mode for " + getClass().getSimpleName()); }
    protected Mode epochMode() { throw new UnsupportedOperationException("No epoch mode for " + getClass().getSimpleName()); }
    protected Mode armMode() { throw new UnsupportedOperationException("No arm mode for " + getClass().getSimpleName()); }

    protected Mode selectMode() {
        Mode newMode;
        if (getArm() != null) {
            newMode = armMode();
        } else if (getEpoch() != null) {
            newMode = epochMode();
        } else {
            newMode = studyMode();
        }
        return newMode;
    }

    protected static interface Mode {
        String getRelativeViewName();
        Map<String, Object> getModel();
        void performEdit();
    }

    ////// REVISED-TO-CURRENT versions of bound props

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

    public void setDaoFinder(DaoFinder daoFinder) {
        this.daoFinder = daoFinder;
    }
}
