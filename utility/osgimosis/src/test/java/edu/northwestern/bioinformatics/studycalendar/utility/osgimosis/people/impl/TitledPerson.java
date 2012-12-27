/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Hat;

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

    public Hat getHat() {
        return person.getHat();
    }
}
