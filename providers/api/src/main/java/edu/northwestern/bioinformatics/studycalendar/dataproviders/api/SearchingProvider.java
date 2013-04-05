/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.api;

import edu.northwestern.bioinformatics.studycalendar.domain.Providable;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public interface SearchingProvider<D extends Providable> {
    List<D> search(String partialName);
}
