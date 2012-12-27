/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * @author Rhett Sutphin
 */
public class PersonProblem extends RuntimeException {
    public PersonProblem() { }
    public PersonProblem(String message) { super(message); }
}
