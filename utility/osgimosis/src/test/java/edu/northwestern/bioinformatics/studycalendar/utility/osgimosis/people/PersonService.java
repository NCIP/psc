/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

import java.awt.*;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 * @author Rhett Sutphin
 */
public interface PersonService {
    Person createPieMaker();
    Person createPrivateInvestigator();
    Collection<Person> createSeveral();
    List<Person> createList();
    Set<Person> createUnique();
    SortedSet<Person> createOrdered();
    Map<Person, Integer> createPieCount();
    Hashtable<Person, Integer> createAwfulLegacyPieCount();
    Person[] createArray();
    int[] createNameLengths();

    Person setTitle(String title, Person person);
    Person pickOne(List<Person> people);
    Person pickOne(Person[] people);

    Person same(Person person);

    void problem() throws PersonProblem;

    String capsKind(Person person);
    Color hatColor(Person person);

    boolean equals(Person p1, Person p2);

    Collection<Person> findByType(Class kind);
}
