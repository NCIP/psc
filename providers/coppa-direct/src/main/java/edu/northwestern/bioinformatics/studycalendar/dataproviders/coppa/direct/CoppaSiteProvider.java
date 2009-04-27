package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.coppa.po.Organization;
import gov.nih.nci.coppa.services.entities.organization.client.OrganizationClient;
import org.apache.axis.types.URI;
import org.iso._21090.ENON;
import org.iso._21090.ENXP;
import org.iso._21090.EntityNamePartType;
import org.iso._21090.II;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class CoppaSiteProvider implements SiteProvider {
    private static final String TEST_ENDPOINT =
        "http://ctms-services-integration.nci.nih.gov/wsrf/services/cagrid/Organization";
    public static final String ORGANIZATION_II_ROOT = "2.16.840.1.113883.3.26.4.2";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private OrganizationClient client;

    public CoppaSiteProvider() {
        try {
            // Temporary
            setOrganizationClient(new OrganizationClient(TEST_ENDPOINT));
        } catch (URI.MalformedURIException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public Site getSite(String assignedIdentifier) {
        Organization example = new Organization();
        II ii = new II();
        ii.setRoot(ORGANIZATION_II_ROOT);
        ii.setExtension(assignedIdentifier);
        example.setIdentifier(ii);

        Organization[] raw = search(example);
        if (raw != null && raw.length > 0) {
            return createSite(raw[0]);
        } else {
            return null;
        }
    }

    public List<Site> search(String partialName) {
        Organization example = createNameExample(partialName);

        Organization[] raw = search(example);
        if (raw == null) {
            return Collections.emptyList();
        } else {
            List<Site> results = new ArrayList<Site>(raw.length);
            for (Organization organization : raw) {
                results.add(createSite(organization));
            }
            return results;
        }
    }

    // package level for testing
    Organization createNameExample(String partialName) {
        Organization example = new Organization();
        ENON name = new ENON();
        ENXP namePart = new ENXP();
        namePart.setValue(partialName);
        namePart.setType(EntityNamePartType.DEL);
        name.getPart().add(namePart);
        example.setName(name);
        return example;
    }

    private Site createSite(Organization organization) {
        Site site = new Site();
        site.setName(organization.getName().getPart().get(0).getValue());
        site.setAssignedIdentifier(organization.getIdentifier().getExtension());
        return site;
    }

    private Organization[] search(Organization criteria) {
        try {
            return client.search(criteria);
        } catch (RemoteException e) {
            log.error("COPPA organization search failed", e);
            return new Organization[0];
        }
    }

    public void setOrganizationClient(OrganizationClient client) {
        this.client = client;
    }
}
