package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedSchedule;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.utils.ExpandingList;

import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

/**
 * @author Rhett Sutphin
 */
public class NewStudyCommand {
    private String studyName;
    private List<String> epochNames;
    /** 2D list.  1st D is epoch, 2nd is list of arm names for the epoch. */
    private List<List<String>> armNames;
    /** A list of flags, one per epoch, indicating whether the epoch has multiple arms */
    private List<Boolean> arms;

    public NewStudyCommand() {
        epochNames = new ExpandingList<String>(new ArrayList<String>());
        epochNames.add("");
        arms = new ExpandingList<Boolean>(
            new ExpandingList.StaticFiller<Boolean>(false),
            new ArrayList<Boolean>());
        armNames = new ExpandingList<List<String>>(
            new ExpandingList.Filler<List<String>>() {
                public List<String> createNew(int index) {
                    return new ExpandingList<String>();
                }
            },
            new ArrayList<List<String>>()
        );
    }

    public Study createStudy() {
        Study study = new Study();
        study.setName(getStudyName());
        PlannedSchedule schedule = new PlannedSchedule();
        study.setPlannedSchedule(schedule);

        for (int i = 0; i < getEpochNames().size(); i++) {
            String epochName = getEpochNames().get(i);

            Epoch e = new Epoch();
            e.setName(epochName);

            if (hasMultipleArms(i)) {
                for (int j = 0; j < armNames.get(i).size(); j++) {
                    String armName = armNames.get(i).get(j);
                    Arm arm = new Arm();
                    arm.setName(armName);
                    e.addArm(arm);
                }
            } else {
                Arm singleArm = new Arm();
                singleArm.setName(epochName);
                e.addArm(singleArm);
            }

            schedule.addEpoch(e);
        }

        return study;
    }

    private boolean hasMultipleArms(int epochIndex) {
        return getArms().get(epochIndex) != null && getArms().get(epochIndex);
    }

    // TODO: validation

    public String getStudyName() {
        return studyName;
    }

    public void setStudyName(String studyName) {
        this.studyName = studyName;
    }

    public List<String> getEpochNames() {
        return epochNames;
    }

    // Can't expose type parameter due to spring bug SPR-2509
    public List/*<List<String>>*/ getArmNames() {
        return armNames;
    }

    public List<Boolean> getArms() {
        return arms;
    }
}
