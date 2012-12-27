/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

/**
 * By implementing this interface, a domain class indicates that it has
 * a simple string identity, in addition to the standard internal
 * ({@link gov.nih.nci.cabig.ctms.domain.DomainObject#getId}) and external
 * ({@link gov.nih.nci.cabig.ctms.domain.GridIdentifiable#getGridId})
 * surrogate keys.
 * <p>
 * Unlike {@link edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed},
 * the key must be unique across the system.  A class may implement both this interface
 * and {@link edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed}.
 *
 * @author Rhett Sutphin
 */
public interface UniquelyKeyed {
    String getUniqueKey();
}
