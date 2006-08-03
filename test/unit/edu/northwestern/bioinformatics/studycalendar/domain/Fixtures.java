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

    private Fixtures() { }
}
