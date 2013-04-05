/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.ihub;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import gov.nih.nci.cagrid.caxchange.client.CaXchangeRequestProcessorClient;
import gov.nih.nci.caxchange.Credentials;
import gov.nih.nci.caxchange.Message;
import gov.nih.nci.caxchange.Metadata;
import gov.nih.nci.caxchange.Request;
import gov.nih.nci.caxchange.ResponseMessage;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.axis.message.MessageElement;
import org.apache.axis.types.URI;
import org.globus.gsi.GlobusCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class IntegrationHubExecutor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static String DELEGATED_CREDENTIAL_XML_USER_ATTRIBUTE = "cagrid.delegated-credential.xml";
    private static String DELEGATED_CREDENTIAL_OBJ_USER_ATTRIBUTE = "cagrid.delegated-credential.value";

    public Object execute(String caXchangeEndpoint, IntegrationHubCoppaTask task) {
        Message requestMessage = createCaXchangeRequestMessage(task);
        if (requestMessage == null) {
            log.error("Failed to create iHub request message.  Aborting.");
            return null;
        }
        if (log.isTraceEnabled()) {
            log.trace("iHub request is\n{}",
                XmlHelper.axisObjectToXmlString(requestMessage, CaXchangeRequestProcessorClient.class));
        }

        ResponseMessage responseMessage = processRequest(caXchangeEndpoint, requestMessage);
        if (responseMessage == null) {
            log.error("Request processing failed.  Aborting.");
            return null;
        }

        List<MessageElement> responseElements = parseResponse(responseMessage);
        if (responseElements == null) {
            log.debug("Response contained no response object.");
            return null;
        }

        try {
            return task.extractResponse(responseElements);
        } catch (Exception e) {
            log.error("Deserializing response from iHub failed.  Aborting.", e);
            return null;
        }
    }

    private CaXchangeRequestProcessorClient createClient(String caXchangeEndpoint) {
        try {
            GlobusCredential dc = getDelegatedCredential();
            if (dc == null) {
                return null;
            }
            return new CaXchangeRequestProcessorClient(caXchangeEndpoint, dc);
        } catch(URI.MalformedURIException e) {
            log.error("Could not parse configured endpoint URI: " + caXchangeEndpoint, e);
            return null;
        } catch(RemoteException e) {
            log.error("Failed to create iHub client", e);
            return null;
        }
    }

    private GlobusCredential getDelegatedCredential() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            log.error("No user authenticated.  Cannot communicate with iHub.");
            return null;
        }
        Object principal = auth.getPrincipal();
        if (principal == null) {
            log.error("No principal for authentication token (this shouldn't be possible).");
            return null;
        } else if (!(principal instanceof PscUser)) {
            log.error("Authenticated principal {} is not a PSC User object; it is a {}.  Cannot extract credential to use with iHub.  (This is indicates a bug in your AuthenticationSystem.)",
                principal, principal.getClass().getName());
            return null;
        }

        PscUser user = (PscUser) principal;
        GlobusCredential found = (GlobusCredential) user.getAttribute(DELEGATED_CREDENTIAL_OBJ_USER_ATTRIBUTE);
        if (found == null) {
            log.error("No delegated credential available in the authentication token under attribute '" +
                DELEGATED_CREDENTIAL_OBJ_USER_ATTRIBUTE + "'.  Cannot communicate with iHub.");
            return null;
        } else {
            return found;
        }
    }

    private Message createCaXchangeRequestMessage(IntegrationHubCoppaTask task) {
        Metadata md = task.createMetadata();
        md.setCredentials(new Credentials()); // Work around bug in CIH
        return new Message(md, new Request(task.createPayload()));
    }

    private ResponseMessage processRequest(String endpoint, Message requestMessage) {
        // create caXchange client
        CaXchangeRequestProcessorClient client = createClient(endpoint);
        if (client == null) {
            log.error("Failed to create iHub client.  Will not make request.");
            return null;
        }

        try {
            return client.processRequestSynchronously(requestMessage);
        } catch (RemoteException e) {
            log.error("Executing request against iHub failed", e);
            return null;
        }
    }

    @SuppressWarnings({ "unchecked" })
    private List<MessageElement> parseResponse(ResponseMessage responseMessage) {
        if (!"SUCCESS".equals(responseMessage.getResponse().getResponseStatus().getValue())) {
            log.error("iHub indicated that execution failed.  Aborting.");
            return null;
        }
        // only one top-level response element in all cases
        MessageElement top = responseMessage.getResponse().getTargetResponse()[0].getTargetBusinessMessage().get_any()[0];
        if ("Array".equals(top.getLocalName())) {
            return top.getChildren();
        } else {
            return Arrays.asList(top);
        }
    }
}
