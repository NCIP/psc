package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;

/**
 * @author Moses Hohman
 */
public class NewPeriodCommand extends Period {
    private Integer armId;

    public Integer getArmId() {
        return armId;
    }

    public void setArmId(Integer armId) {
        this.armId = armId;
    }
}
