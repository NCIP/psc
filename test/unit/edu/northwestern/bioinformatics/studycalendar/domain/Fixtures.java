package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Map;
import java.util.HashMap;

/**
 * @author Rhett Sutphin
 */
public class Fixtures {
    private static final Map<Integer, ActivityType> ACTIVITY_TYPES = new HashMap<Integer, ActivityType>();

    static {
        addActivityType(1, "therapeutic/interventional");
        addActivityType(2, "observational");
        addActivityType(3, "correlative");
        addActivityType(4, "ancillary");
        addActivityType(5, "prevention");
        addActivityType(6, "screening");
        addActivityType(7, "early detection");
        addActivityType(8, "supportive care");
        addActivityType(9, "epidemiologic");
        addActivityType(10, "biobanking");
    }

    private static void addActivityType(int id, String name) {
        ActivityType t = new ActivityType();
        t.setId(id);
        t.setName(name);
        ACTIVITY_TYPES.put(t.getId(), t);
    }

    public static <T extends AbstractDomainObject> T setId(Integer id, T target) {
        target.setId(id);
        return target;
    }

    public static ActivityType getActivityType(int id) {
        return ACTIVITY_TYPES.get(id);
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
        activity.setType(getActivityType(9));
        event.setActivity(activity);
        event.setDay(day);
        return event;
    }

    public static Epoch createEpoch(String name, String... armNames) {
        Epoch epoch = new Epoch();
        epoch.setName(name);
        if (armNames.length == 0) {
            epoch.addArm(createNamedInstance(name, Arm.class));
        } else {
            for (String armName : armNames) {
                epoch.addArm(createNamedInstance(armName, Arm.class));
            }
        }
        return epoch;
    }

    public static StudySite createStudySite(Study study, Site site) {
        StudySite studySite = new StudySite();
        if (study != null) study.addStudySite(studySite);
        studySite.setSite(site);
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
