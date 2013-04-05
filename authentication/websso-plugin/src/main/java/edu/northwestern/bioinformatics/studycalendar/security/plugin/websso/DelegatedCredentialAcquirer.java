/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.plugin.websso;

import org.globus.gsi.GlobusCredential;
import org.cagrid.gaards.cds.delegated.stubs.types.DelegatedCredentialReference;
import org.cagrid.gaards.cds.client.CredentialDelegationServiceClient;
import org.cagrid.gaards.cds.client.DelegatedCredentialUserClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.nih.nci.cagrid.common.Utils;

import java.io.StringReader;

/**
 * @author Rhett Sutphin
 */
public class DelegatedCredentialAcquirer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private String xml;
    private String hostCertificateFilename;
    private String hostKeyFilename;

    public DelegatedCredentialAcquirer(String xml, String hostCertificateFilename, String hostKeyFilename) {
        this.xml = xml;
        this.hostCertificateFilename = hostCertificateFilename;
        this.hostKeyFilename = hostKeyFilename;
    }

    protected GlobusCredential acquire() throws Exception {
        log.debug("Attempting to load delegated credential");
        log.trace("- Building host credential out of cert={} and key={}",
            hostCertificateFilename, hostKeyFilename);
        GlobusCredential hostCredential = new GlobusCredential(hostCertificateFilename, hostKeyFilename);
        log.trace("* hostCredential={}", hostCredential);
        log.trace("- Deserializing reference \n{}", xml);
        DelegatedCredentialReference delegatedCredentialReference =
            (DelegatedCredentialReference) Utils.deserializeObject(
                new StringReader(xml), DelegatedCredentialReference.class,
                CredentialDelegationServiceClient.class.getResourceAsStream("client-config.wsdd"));
        log.trace("* reference={}", delegatedCredentialReference);
        log.trace("* reference.endpointReference={}",
            delegatedCredentialReference.getEndpointReference());
        log.trace("* reference.endpointReference.address={}",
            delegatedCredentialReference.getEndpointReference().getAddress());
        log.trace("* reference.endpointReference.address.host={}",
            delegatedCredentialReference.getEndpointReference().getAddress().getHost());
        log.trace("* reference.endpointReference.address.path={}",
            delegatedCredentialReference.getEndpointReference().getAddress().getPath());

        log.trace("- Getting delegated credential from reference");
        DelegatedCredentialUserClient delegatedCredentialUserClient =
            new DelegatedCredentialUserClient(delegatedCredentialReference, hostCredential);
        GlobusCredential userCredential = delegatedCredentialUserClient.getDelegatedCredential();
        log.trace("* userCredential={}", userCredential);
        log.trace("* uc.identity={}", userCredential.getIdentity());
        log.trace("* uc.issuer={}", userCredential.getIssuer());
        log.trace("* uc.subject={}", userCredential.getSubject());
        return userCredential;
    }
}
