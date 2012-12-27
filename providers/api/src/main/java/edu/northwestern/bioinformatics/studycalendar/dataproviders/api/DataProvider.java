/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

/**
 * Base interface for all providers.  There's no value in implementing this interface alone;
 * implement one of its subinterfaces.
 *
 * @author Rhett Sutphin
 */
public interface DataProvider {
    /**
     * A unique string that will be used to distinguish instances obtained from this provider
     * from instances created locally or obtained from other providers.  Must be less than
     * 250 characters.
     */
    String providerToken();
}
