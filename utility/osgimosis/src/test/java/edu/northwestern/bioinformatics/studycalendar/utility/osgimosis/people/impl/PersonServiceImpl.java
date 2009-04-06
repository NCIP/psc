package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonProblem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonService;

import java.util.Arrays;
import java.util.Collection;
import java.awt.*;

/**
 * @author Rhett Sutphin
 */
public class PersonServiceImpl implements PersonService {
    public Person createPieMaker() {
        return new PieMaker("Ned");
    }

    public Person createPrivateInvestigator() {
        return new PrivateInvestigator("Emerson");
    }

    public Collection<Person> createSeveral() {
        return Arrays.asList(
            createPieMaker(), createPrivateInvestigator(), new PieMaker("Chuck")
        );
    }

    public Person setTitle(String title, Person person) {
        return new TitledPerson(title, person);
    }

    public Person same(Person person) {
        return person;
    }

    public void problem() throws PersonProblem {
        throw new PersonProblem("Implementation problem");
    }

    public String capsKind(Person person) {
        return person.getKind().toUpperCase();
    }

    public Color hatColor(Person person) {
        return person.getHat() == null ? null : person.getHat().getColor();
    }

    public boolean equals(Person p1, Person p2) {
        return p1.equals(p2);
    }

    private static class PrivateInvestigator extends AbstractPerson {
        public PrivateInvestigator(String name) {
            super(name, "PI");
        }

        // Define these here so that the declaring class is private

        @Override
        public String getKind() {
            return super.getKind();
        }

        @Override
        public String toString() {
            return new StringBuilder(getClass().getSimpleName()).
                append('[').append(getName()).append(']').toString();
        }
    }
}
