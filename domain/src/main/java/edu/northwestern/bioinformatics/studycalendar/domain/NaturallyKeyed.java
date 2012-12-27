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
 * The key is understood to be unique within the context of the parent
 * of the keyed object.  For example, the natural key for an activity
 * need only be unique within its source.
 *
 * @see UniquelyKeyed
 * @author Rhett Sutphin
 */
public interface NaturallyKeyed {
    String getNaturalKey();
}
