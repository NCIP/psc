package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.nwu.bioinformatics.commons.DateUtils;

import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.Scheduled;
import edu.northwestern.bioinformatics.studycalendar.domain.scheduledeventstate.ScheduledEventState;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * @author Rhett Sutphin
 */
public class Fixtures {
    public static <T extends DomainObject> T setId(Integer id, T target) {
        target.setId(id);
        return target;
    }

    public static <T extends GridIdentifiable> T setGridId(String gridId, T target) {
        target.setGridId(gridId);
        return target;
    }

    public static Period createPeriod(String name, int startDay, int dayCount, int repetitions) {
        return createPeriod(name, startDay, Duration.Unit.day, dayCount, repetitions);
    }

    public static Period createPeriod(String name, int startDay, Duration.Unit dUnit, int dQuantity, int repetitions) {
        Period p = new Period();
        p.setName(name);
        p.setStartDay(startDay);
        p.getDuration().setUnit(dUnit);
        p.getDuration().setQuantity(dQuantity);
        p.setRepetitions(repetitions);
        return p;
    }

    public static PlannedEvent createPlannedEvent(String activityName, int day) {
        PlannedEvent event = new PlannedEvent();
        Activity activity = createNamedInstance(activityName, Activity.class);
        activity.setType(ActivityType.LAB_TEST);
        event.setActivity(activity);
        event.setDay(day);
        return event;
    }

    public static PlannedEvent createPlannedEvent(String activityName, int day, String details) {
        PlannedEvent event = createPlannedEvent(activityName, day);
        event.setDetails(details);
        return event;
    }

    public static Study createSingleEpochStudy(String studyName, String epochName, String... armNames) {
        Study study = createNamedInstance(studyName, Study.class);
        study.setPlannedCalendar(new PlannedCalendar());
        study.getPlannedCalendar().addEpoch(Epoch.create(epochName, armNames));
        return study;
    }

    public static StudySite createStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) study.addStudySite(studySite);
        if (site != null) site.addStudySite(studySite);
        return studySite;
    }

    public static Participant createParticipant(String firstName, String lastName) {
        Participant p = new Participant();
        p.setFirstName(firstName);
        p.setLastName(lastName);
        return p;
    }

    public static ScheduledArm createScheduledArm(Arm arm) {
        ScheduledArm scheduledArm = new ScheduledArm();
        scheduledArm.setArm(arm);
        return scheduledArm;
    }

    public static ScheduledEvent createScheduledEvent(
        String activityName, int year, int month, int day, ScheduledEventState state
    ) {
        ScheduledEvent event = createScheduledEvent(activityName, year, month, day);
        event.changeState(state);
        return event;
    }

    public static ScheduledEvent createScheduledEvent(String activityName, int year, int month, int day) {
        PlannedEvent baseEvent = createPlannedEvent(activityName, 0);
        ScheduledEvent event = new ScheduledEvent();
        event.setPlannedEvent(baseEvent);
        event.setIdealDate(DateUtils.createDate(year, month, day - 2));
        event.changeState(new Scheduled(null, DateUtils.createDate(year, month, day)));
        return event;
    }

    public static void addEvents(ScheduledArm scheduledArm, ScheduledEvent... events) {
        for (ScheduledEvent event : events) {
            scheduledArm.addEvent(event);
        }
    }

    public static <T extends Named> T createNamedInstance(String name, Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            instance.setName(name);
            return instance;
        } catch (InstantiationException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Creating domain obj of class " + clazz.getName() + " failed", e);
        }
    }

    // static class
    private Fixtures() { }
}
