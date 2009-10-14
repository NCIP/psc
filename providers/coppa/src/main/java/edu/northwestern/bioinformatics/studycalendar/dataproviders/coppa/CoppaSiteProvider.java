package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaSiteProvider.OrganizationIdentifier.fromAssignedIdentifier;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import gov.nih.nci.coppa.po.Id;
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
 * @author John Dzak
 */
public class CoppaSiteProvider implements SiteProvider {
    private static final String TEST_ENDPOINT =
        "http://ctms-services-po-2-2-integration.nci.nih.gov/wsrf/services/cagrid/Organization";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private OrganizationClient client;

    public CoppaSiteProvider() {
        try {
            // Temporary
            setClient(new OrganizationClient(TEST_ENDPOINT));
        } catch (URI.MalformedURIException e) {
            throw new RuntimeException(e);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Site> getSites(List<String> assignedIdentifiers) {
        List<Site> sites = new ArrayList<Site>();

        for (Id id: createIds(assignedIdentifiers)) {
            Organization o = searchById(id);
            sites.add(o == null ? null : createSite(o));
        }

        return sites;
    }

    public List<Site> search(String partialName) {
        Organization example = createNameExample(partialName);

        Organization[] raw = searchByOrganization(example);
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

    public String providerToken() {
        return "coppa";
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

    private Organization[] searchByOrganization(Organization criteria) {
        try {
            return client.search(criteria);
        } catch (RemoteException e) {
            log.error("COPPA organization search failed", e);
            return new Organization[0];
        }
    }

    private Organization searchById(Id id) {
        try {
            return client.getById(id);
        } catch (RemoteException e) {
            log.error("COPPA organization search failed", e);
            return null;
        }
    }

    private Id[] createIds(List<String> assignedidentifiers) {
        List<Id> iis = new ArrayList<Id>();
        for(String ai : assignedidentifiers) {
            iis.add(fromAssignedIdentifier(ai).createId());
        }
        return iis.toArray(new Id[0]);
    }


    public void setClient(OrganizationClient client) {
        this.client = client;
    }

    public static class OrganizationIdentifier {
        public static final String ORGANIZATION_II_ROOT = "2.16.840.1.113883.3.26.4.2";
        private String identifier;

        public OrganizationIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public static OrganizationIdentifier fromAssignedIdentifier(String val) {
            return new OrganizationIdentifier(val);
        }

        public Id createId() {
            return buildCoppaIdentifier(Id.class);
        }
        public II createII() {
            return buildCoppaIdentifier(II.class);
        }

        private <T extends org.iso._21090.II> T buildCoppaIdentifier(Class<T> clazz) {
            try {
                T inst = clazz.newInstance();
                inst.setRoot(ORGANIZATION_II_ROOT);
                inst.setExtension(identifier);
                return inst;
            } catch (IllegalAccessException e) {
                throw new StudyCalendarError("Inaccessible child class", e);
            } catch (InstantiationException e ) {
                throw new StudyCalendarError("Uninstantiable child class", e);
            }
        }
    }
}
