package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;

/**
 * @author Moses Hohman
 */
public class NewPeriodCommand {
    private Arm arm;
    private Period period;

    public NewPeriodCommand() {
        period = new Period();
    }

    public void apply() {
        getArm().addPeriod(getPeriod());
    }

    public Period getPeriod() {
        return period;
    }

    public Arm getArm() {
        return arm;
    }

    public void setArm(Arm arm) {
        this.arm = arm;
    }
}
