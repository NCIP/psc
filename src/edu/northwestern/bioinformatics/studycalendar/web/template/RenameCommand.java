package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;

/**
 * @author Rhett Sutphin
 */
public class RenameCommand {
    private String value;

    private Study study;
    private Epoch epoch;
    private Arm arm;

    private StudyDao studyDao;

    public RenameCommand(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    ////// LOGIC

    public void apply() {
        if (getStudy() != null) {
            rename(getStudy());
            studyDao.save(getStudy());
        }
        if (getEpoch() != null) {
            renameEpoch();
            studyDao.save(getEpoch().getPlannedCalendar().getStudy());
        }
        if (getArm() != null) {
            renameArm();
            studyDao.save(getArm().getEpoch().getPlannedCalendar().getStudy());
        }
    }

    private void rename(Named named) {
        named.setName(getValue());
    }

    private void renameEpoch() {
        rename(getEpoch());
        if (!getEpoch().isMultipleArms()) {
            rename(getEpoch().getArms().get(0));
        }
    }

    private void renameArm() {
        rename(getArm());
        if (!getArm().getEpoch().isMultipleArms()) {
            rename(getArm().getEpoch());
        }
    }

    ////// BOUND PROPERTIES

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

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
}
