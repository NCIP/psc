package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

/**
 * @author Rhett Sutphin
 */
public class TitledPerson implements Person {
    private String title;
    private Person person;

    public TitledPerson(String title, Person person) {
        this.title = title;
        this.person = person;
    }

    public String getName() {
        return title + ' ' + person.getName();
    }

    public String getKind() {
        return person.getKind();
    }
}
