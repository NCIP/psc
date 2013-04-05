/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import org.apache.axis.types.URI;

/**
 * @author Rhett Sutphin
 */
public interface HubOperation {
    String getServiceType();
    String getOperationName();
    Class<?> getClientClass();
    Class<?> getResponseType();
    URI getNamespaceURI();
}
