package edu.northwestern.bioinformatics.studycalendar.web.delta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.EpochDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.ChangeDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.DeltaDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Change;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.web.template.ModalEditCommand;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
 abstract class AmendEditCommand extends ModalEditCommand {
    private static final Logger log = LoggerFactory.getLogger(AmendEditCommand.class.getName());
    protected StudyDao studyDao;

    protected ChangeDao changeDao;
    protected DeltaDao deltaDao;
    protected EpochDao epochDao;

    protected AmendmentDao amendmentDao;

    private Study study;
    private Epoch epoch;
    private Arm arm;

    private Amendment amendment;
    private Change change;
    private Delta delta;

    public void apply(AmendmentDao amendmentDao, ChangeDao changeDao, DeltaDao deltaDao) {
        setAmendmentDao(amendmentDao);
        setChangeDao(changeDao);
        setDeltaDao(deltaDao);
        performEdit();
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
    }

    ////// BOUND PROPERTIES

    public Study getStudy() {
        return study;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public void setEpoch(Epoch epoch) {
        this.epoch = epoch;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }

    public Amendment getAmendment() {
        return amendment;
    }

    public void setAmendment(Amendment amendment) {
        this.amendment = amendment;
    }

    public Change getChange() {
        return change;
    }

    public void setChange(Change change) {
        this.change = change;
    }

    public Delta getDelta() {
        return delta;
    }

    public void setDelta(Delta delta) {
        this.delta = delta;
    }

     ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao ;
    }

    @Required
    public void setDeltaDao(DeltaDao deltaDao) {
        this.deltaDao = deltaDao ;
    }

    @Required
    public void setChangeDao(ChangeDao changeDao) {
        this.changeDao = changeDao ;
    }

    @Required
    public void setEpochDao(EpochDao epochDao) {
        this.epochDao = epochDao ;
    }
}
