package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbContext {
    private Study study;
    private PlannedCalendar plannedCalendar;
    private Epoch epoch;
    private Arm arm;
    private Period period;
    private PlannedEvent plannedEvent;

    private ScheduledCalendar scheduledCalendar;
    private ScheduledArm scheduledArm;
    private ScheduledEvent scheduledEvent;

    public BreadcrumbContext() { }

    public static BreadcrumbContext create(DomainObject basis) {
        BreadcrumbContext context = new BreadcrumbContext();
        if (basis != null) {
            if (basis instanceof Study) context.setStudy((Study) basis);
            else if (basis instanceof PlannedCalendar) context.setPlannedCalendar((PlannedCalendar) basis);
            else if (basis instanceof Epoch) context.setEpoch((Epoch) basis);
            else if (basis instanceof Arm) context.setArm((Arm) basis);
            else if (basis instanceof Period) context.setPeriod((Period) basis);
            else throw new UnsupportedOperationException("No setter for property of type " + basis.getClass().getName());
        }
        return context;
    }

    public void setStudy(Study study) {
        this.study = study;
    }

    public void setPlannedCalendar(PlannedCalendar plannedCalendar) {
        setStudy(plannedCalendar.getStudy());
        this.plannedCalendar = plannedCalendar;
    }

    public void setEpoch(Epoch epoch) {
        setPlannedCalendar(epoch.getPlannedCalendar());
        this.epoch = epoch;
    }

    public void setArm(Arm arm) {
        setEpoch(arm.getEpoch());
        this.arm = arm;
    }

    public void setPeriod(Period period) {
        setArm(period.getArm());
        this.period = period;
    }

    // TODO: more setters, as needed

    ////// BEAN PROPERTIES

    public Study getStudy() {
        return study;
    }

    public PlannedCalendar getPlannedCalendar() {
        return plannedCalendar;
    }

    public Epoch getEpoch() {
        return epoch;
    }

    public Arm getArm() {
        return arm;
    }

    public Period getPeriod() {
        return period;
    }

    public PlannedEvent getPlannedEvent() {
        return plannedEvent;
    }

    public ScheduledCalendar getScheduledCalendar() {
        return scheduledCalendar;
    }

    public ScheduledArm getScheduledArm() {
        return scheduledArm;
    }

    public ScheduledEvent getScheduledEvent() {
        return scheduledEvent;
    }
}
