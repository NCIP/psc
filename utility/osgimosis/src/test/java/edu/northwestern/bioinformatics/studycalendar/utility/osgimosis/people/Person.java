/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utility.osgimosis.people;

/**
 * @author Rhett Sutphin
 */
public interface Person {
    String getName();
    String getKind();
    Hat getHat();
}
