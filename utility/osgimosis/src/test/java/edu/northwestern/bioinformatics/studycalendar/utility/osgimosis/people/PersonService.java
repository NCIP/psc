package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.awt.*;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public interface PersonService {
    Person createPieMaker();
    Person createPrivateInvestigator();
    Collection<Person> createSeveral();
    Person[] createArray();
    int[] createNameLengths();

    Person setTitle(String title, Person person);

    Person same(Person person);

    void problem() throws PersonProblem;

    String capsKind(Person person);
    Color hatColor(Person person);

    boolean equals(Person p1, Person p2);
}
