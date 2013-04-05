/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.DefaultPerson;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PeopleByName;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonProblem;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.PersonService;

import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
        return Arrays.asList(createArray());
    }

    public List<Person> createList() {
        return new LinkedList<Person>(createSeveral());
    }

    public SortedSet<Person> createOrdered() {
        SortedSet<Person> set = new TreeSet<Person>(PeopleByName.INSTANCE);
        set.addAll(createSeveral());
        return set;
    }

    public Set<Person> createUnique() {
        return new LinkedHashSet<Person>(createList());
    }

    public Person[] createArray() {
        return new Person[] {
            createPieMaker(), createPrivateInvestigator(), new PieMaker("Chuck")
        };
    }

    public Map<Person, Integer> createPieCount() {
        Map<Person, Integer> pieCount = new HashMap<Person, Integer>();
        pieCount.put(createPieMaker(), 8);
        pieCount.put(createPrivateInvestigator(), 0);
        return pieCount;
    }

    public Hashtable<Person, Integer> createAwfulLegacyPieCount() {
        return new Hashtable<Person, Integer>(createPieCount());
    }

    public int[] createNameLengths() {
        Person[] people = createArray();
        int[] lengths = new int[people.length];
        for (int i = 0; i < people.length; i++) {
            lengths[i] = people[i].getName().length();
        }
        return lengths;
    }

    public Person setTitle(String title, Person person) {
        return new TitledPerson(title, person);
    }

    public Person pickOne(List<Person> people) {
        return people.get(people.size() - 1);
    }

    public Person pickOne(Person[] people) {
        return people[0];
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

    public Collection<Person> findByType(Class kind) {
        if (kind.isAssignableFrom(DefaultPerson.class)) {
            return Collections.<Person>singleton(new DefaultPerson("Chuck", "Dead"));
        } else {
            return Collections.emptySet();
        }
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
