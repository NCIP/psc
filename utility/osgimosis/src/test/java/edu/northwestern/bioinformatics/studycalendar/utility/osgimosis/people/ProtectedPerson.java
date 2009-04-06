package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * A person with a protected default constructor.
 *
 * @author Rhett Sutphin
 */
public class ProtectedPerson extends DefaultPerson {
    protected ProtectedPerson() { }

    public static Person create() { return new ProtectedPerson(); }
}
