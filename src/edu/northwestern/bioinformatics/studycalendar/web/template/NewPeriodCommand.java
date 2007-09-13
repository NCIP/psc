package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.DeltaService;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;

/**
 * @author Moses Hohman
 */
public class NewPeriodCommand implements PeriodCommand {
    private Arm arm;
    private Period period;
    private AmendmentService amendmentService;

    public NewPeriodCommand(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
        period = new Period();
    }

    // TODO: this is going to have to be an arbitrary revision at some point
    // (i.e. for Customizations)
    public void apply() {
        amendmentService.updateDevelopmentAmendment(getArm(), Add.create(getPeriod()));
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
