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
