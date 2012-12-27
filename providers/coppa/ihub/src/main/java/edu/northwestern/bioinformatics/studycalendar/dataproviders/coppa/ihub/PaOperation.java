/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.coppa.services.pa.StudyProtocol;
import gov.nih.nci.coppa.services.pa.StudySite;
import gov.nih.nci.coppa.services.pa.studysiteservice.client.StudySiteServiceClient;
import gov.nih.nci.coppa.services.pa.studyprotocolservice.client.StudyProtocolServiceClient;
import org.apache.axis.types.URI;

/**
 * @author Rhett Sutphin
 */
public enum PaOperation implements HubOperation {
    GET_STUDY_PROTOCOL(              "getStudyProtocol",  StudyProtocol.class,   StudyProtocolServiceClient.class),
    STUDY_PROTOCOL_SEARCH(           "search",            StudyProtocol[].class, StudyProtocolServiceClient.class),
    GET_STUDY_SITES_BY_PROTOCOL(     "getByStudyProtocol",StudySite[].class,     StudySiteServiceClient.class),
    SEARCH_STUDY_SITES_BY_STUDY_SITE("search",            StudySite[].class,     StudySiteServiceClient.class);

    private String operationName;
    private Class<?> responseType;
    private Class<?> clientClass;

    private PaOperation(String operationName, Class<?> responseType, Class<?> clientClass) {
        this.operationName = operationName;
        this.responseType = responseType;
        this.clientClass = clientClass;
    }

    public String getServiceType() {
        return getClientClass().getSimpleName().
            replaceAll("ServiceClient", "").
            replaceAll("([a-z])([A-Z])", "$1_$2").
            toUpperCase()
            ;
    }

    public String getOperationName() {
        return operationName;
    }

    public Class<?> getClientClass() {
        return clientClass;
    }

    public Class<?> getResponseType() {
        return responseType;
    }

    public URI getNamespaceURI() {
        try {
            return new URI("http://pa.services.coppa.nci.nih.gov");
        } catch (URI.MalformedURIException e) {
            throw new StudyCalendarError("It isn't malformed", e);
        }
    }
}
