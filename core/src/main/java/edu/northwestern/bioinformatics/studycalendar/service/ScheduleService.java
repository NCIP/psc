package edu.northwestern.bioinformatics.studycalendar.service;

import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledActivityMode;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Revision;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledactivitystate.*;
import org.springframework.beans.factory.annotation.Required;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.Date;

import gov.nih.nci.cabig.ctms.lang.StringTools;

/**
 * @author Rhett Sutphin
 */
public class ScheduleService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private SubjectService subjectService;
    private static final String MISSED = "missed";
    private static final String SCHEDULED = "scheduled";
    private static final String OCCURRED = "occurred";
    private static final String CANCELED = "canceled";
    private static final String CONDITIONAL = "conditional";
    private static final String NOT_APPLICABLE = "not-applicable";

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

    public ScheduledActivityState createScheduledActivityState(String state, Date date, String reason){
        ScheduledActivityState scheduledActivityState = null;
        if (state == null || StringUtils.isEmpty(state)) {
            return null;
        } else if (state.equals(CONDITIONAL)) {
            scheduledActivityState = new Conditional();
        } else if (state.equals(OCCURRED)) {
            scheduledActivityState = new Occurred();
        } else if (state.equals(SCHEDULED)) {
            scheduledActivityState = new Scheduled();
        } else if (state.equals(MISSED)) {
            scheduledActivityState = new Missed();
        } else if (state.equals(NOT_APPLICABLE)) {
            scheduledActivityState = new NotApplicable();
        } else if (state.equals(CANCELED)) {
            scheduledActivityState = new Canceled();
        } else {
            return scheduledActivityState;
        }

        if (date != null) {
            scheduledActivityState.setDate(date);
        }
        if (reason != null && StringUtils.isNotEmpty(reason)) {
            scheduledActivityState.setReason(reason);
        }

        return scheduledActivityState;
    }

    ////// CONFIGURATION

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }
}
