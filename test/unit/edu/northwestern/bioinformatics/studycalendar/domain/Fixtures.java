package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * @author Rhett Sutphin
 */
public class Fixtures {
    public static <T extends AbstractDomainObject> T setId(Integer id, T target) {
        target.setId(id);
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
