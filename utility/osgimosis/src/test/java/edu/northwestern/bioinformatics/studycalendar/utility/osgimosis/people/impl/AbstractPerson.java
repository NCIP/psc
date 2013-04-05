/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.impl;

import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Hat;
import edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people.Person;

/**
 * @author Rhett Sutphin
 */
public class AbstractPerson implements Person {
    private String name, kind;
    private Hat hat;

    public AbstractPerson(String name, String kind) {
        this.name = name;
        this.kind = kind;
        this.hat = null;
    }

    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public Hat getHat() {
        return hat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person that = (Person) o;

        if (kind != null ? !kind.equals(that.getKind()) : that.getKind() != null) return false;
        if (name != null ? !name.equals(that.getName()) : that.getName() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
    }
}
