package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

import java.util.List;
import java.util.LinkedList;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private String studyName;
    private List<String> armNames = new LinkedList<String>();
    private boolean arms = false;

    public Study createStudy() {
        Study study = new Study();
        study.setName(getStudyName());
        if (getArms()) {
            for (String armName : getArmNames()) {
                Arm arm = new Arm();
                arm.setName(armName);
                study.getArms().add(arm);
            }
        }
        return study;
    }

    // TODO: validation

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public boolean getArms() {
        return arms;
    }

    public void setArms(boolean arms) {
        this.arms = arms;
    }

    public List<String> getArmNames() {
        return armNames;
    }
}
