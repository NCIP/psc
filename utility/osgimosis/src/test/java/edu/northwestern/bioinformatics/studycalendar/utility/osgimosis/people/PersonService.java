package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.util.Collection;
import java.util.List;
import java.awt.*;

/**
 * @author Rhett Sutphin
 */
public interface PersonService {
    Person createPieMaker();
    Person createPrivateInvestigator();
    Collection<Person> createSeveral();
    List<Person> createList();
    Person[] createArray();
    int[] createNameLengths();

    Person setTitle(String title, Person person);

    Person same(Person person);

    void problem() throws PersonProblem;

    String capsKind(Person person);
    Color hatColor(Person person);

    boolean equals(Person p1, Person p2);
}
