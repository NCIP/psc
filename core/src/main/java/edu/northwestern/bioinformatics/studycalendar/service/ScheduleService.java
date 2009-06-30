package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.ScheduledActivityState;
import gov.nih.nci.cabig.ctms.lang.StringTools;
import org.springframework.beans.factory.annotation.Required;

import java.util.Calendar;

/**
 * @author Rhett Sutphin
 */
public class ScheduleService {
    private SubjectService subjectService;

    /**
     * Shifts the given event by the given number of days, if the event is outstanding.
     * If the new date is a blackout date, it shifts it again until it's not.
     *
     * TODO: might should update idealDate, too
     *
     * @param event
     * @param amount
     * @param source
     */
    public void reviseDate(ScheduledActivity event, int amount, Revision source) {
        if (!event.getCurrentState().getMode().isOutstanding()) return;
        Calendar newDate = Calendar.getInstance();
        ScheduledActivityState currentState = event.getCurrentState();
        newDate.setTime(currentState.getDate());
        newDate.add(Calendar.DAY_OF_YEAR, amount);
        ScheduledActivityState newState;
        if (amount == 0 && event.isConditionalEvent()) {
            newState = ScheduledActivityMode.CONDITIONAL.createStateInstance();
            newState.setReason(createReason(source));
        } else {
            newState = currentState.getMode().createStateInstance();
            newState.setReason(createShiftReason(amount, source));
        }
        newState.setDate(newDate.getTime());
        event.changeState(newState);
        subjectService.avoidBlackoutDates(event);
    }

    private String createShiftReason(int amount, Revision source) {
        return new StringBuilder("Shifted ")
            .append(amount < 0 ? "back" : "forward")
            .append(' ').append(StringTools.createCountString(Math.abs(amount), "day"))
            .append(" in revision ").append(source.getDisplayName())
            .toString();
    }

    private String createReason(Revision source) {
        return new StringBuilder("State change").append(" in revision ").append(source.getDisplayName()).toString();
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
