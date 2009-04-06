package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * A person with no default constructor.
 *
 * @author Rhett Sutphin
 */
public class NonDefaultPerson implements Person {
    private String name;

    public NonDefaultPerson(String name) {
        if (name == null) throw new IllegalArgumentException("Name is required");
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return "non-default";
    }
}
