package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * A person with a private default constructor.
 *
 * @author Rhett Sutphin
 */
public class PrivatePerson extends DefaultPerson {
    private PrivatePerson() { }

    public static Person create() { return new PrivatePerson(); }
}
