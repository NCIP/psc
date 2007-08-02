package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;

import java.util.Map;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public abstract class EditCommand {
    private StudyDao studyDao;
    private Study study;
    private Epoch epoch;
    private Arm arm;

    public void apply() {
        Study target = toSave();
        if (target.getPlannedCalendar().isComplete()) {
            throw new StudyCalendarSystemException(
                "The calendar for " + target.getName() + " is complete, and therefore uneditable");
        } else {
            performEdit();
            studyDao.save(target);
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

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }
}
