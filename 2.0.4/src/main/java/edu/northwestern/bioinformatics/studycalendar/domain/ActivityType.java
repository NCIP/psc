package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.Collection;

/**
 * @author Jaron Sampson
 * @author Rhett Sutphin
 */
public class ActivityType extends AbstractControlledVocabularyObject {

    public static final ActivityType DISEASE_MEASURE = new ActivityType(1, "Disease Measure");
    public static final ActivityType INTERVENTION    = new ActivityType(2, "Intervention");
    public static final ActivityType LAB_TEST        = new ActivityType(3, "Lab Test");
    public static final ActivityType PROCEDURE       = new ActivityType(4, "Procedure");
    public static final ActivityType OTHER           = new ActivityType(5, "Other");

    private ActivityType(int id, String name) {
        super(id, name);
    }

    public static ActivityType getById(int id) {
        return getById(ActivityType.class, id);
    }

    public static Collection<ActivityType> values() {
        return values(ActivityType.class);
    }
}
