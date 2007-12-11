package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.dao.DaoFinder;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.web.delta.RevisionChanges;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;

import java.util.*;

import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private StudySegment studySegment;

    // revised
    private Study revisedStudy;
    private Epoch revisedEpoch;
    private StudySegment revisedStudySegment;

    private static final Logger log = LoggerFactory.getLogger(EditTemplateCommand.class.getName());

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

        Study theRevisedStudy = deltaService.revise(getStudy(), getStudy().getDevelopmentAmendment());
        List<Epoch> epochs = theRevisedStudy.getPlannedCalendar().getEpochs();
        model.put("epochs", epochs);
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
    protected Mode studySegmentMode() { throw new UnsupportedOperationException("No studySegment mode for " + getClass().getSimpleName()); }

    protected Mode selectMode() {
        Mode newMode;
        if (getStudySegment() != null) {
            newMode = studySegmentMode();
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
        if (revisedStudy != null  && (getEpoch() != null || getStudySegment() != null)) {
            for (Epoch e : revisedStudy.getPlannedCalendar().getEpochs()) {
                if (getEpoch() != null && e.getId().equals(getEpoch().getId())) {
                    revisedEpoch = e;
                }
                for (StudySegment a : e.getStudySegments()) {
                    if (getStudySegment() != null && a.getId().equals(getStudySegment().getId())) {
                        revisedStudySegment = a;
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

    public StudySegment getRevisedStudySegment() {
        return revisedStudySegment;
    }

    public PlannedCalendar getSafeEpochParent() {
        return getSafeParent(getEpoch(), getRevisedEpoch());
    }

    public Epoch getSafeStudySegmentParent() {
        return getSafeParent(getStudySegment(), getRevisedStudySegment());
    }

    private <P extends DomainObject> P getSafeParent(PlanTreeNode<P> bound, PlanTreeNode<P> revised) {
        // these casts are safe because this method is only used with Study Segments or Epochs
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

    public StudySegment getStudySegment() {
        return studySegment;
    }

    public void setStudySegment(StudySegment studySegment) {
        this.studySegment = studySegment;
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
