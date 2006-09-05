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

    public static ActivityType getActivityType(int id) {
        return ACTIVITY_TYPES.get(id);
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
