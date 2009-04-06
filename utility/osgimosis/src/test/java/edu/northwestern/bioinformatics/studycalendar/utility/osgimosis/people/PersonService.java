package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * @author Rhett Sutphin
 */
public interface PersonService {
    Person createPieMaker();
    Person createPrivateInvestigator();

    Person setTitle(String title, Person person);

    Person same(Person person);

    void problem() throws PersonProblem;

    String capsKind(Person person);

    boolean equals(Person p1, Person p2);
}
