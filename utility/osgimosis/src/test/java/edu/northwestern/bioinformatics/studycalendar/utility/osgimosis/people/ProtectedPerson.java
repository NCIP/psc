/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * A person with a protected default constructor.
 *
 * @author Rhett Sutphin
 */
public class ProtectedPerson extends DefaultPerson {
    protected ProtectedPerson() { }

    public static Person create() { return new ProtectedPerson(); }
}
