package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonService;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonProblem;

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

    public boolean equals(Person p1, Person p2) {
        return p1.equals(p2);
    }

    private static class PrivateInvestigator extends AbstractPerson {
        public PrivateInvestigator(String name) {
            super(name, "PI");
        }

        // Define this here so that the declaring class is private
        @Override
        public String getKind() {
            return super.getKind();
        }
    }
}
