/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import static edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.CoppaSiteProvider.OrganizationIdentifier.fromAssignedIdentifier;
import edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.helpers.CoppaProviderHelper;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.coppa.po.Id;
import gov.nih.nci.coppa.po.Organization;
import org.iso._21090.ENON;
import org.iso._21090.ENXP;
import org.iso._21090.EntityNamePartType;
import org.iso._21090.II;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 * @author John Dzak
 */
public class CoppaSiteProvider implements SiteProvider {
    private BundleContext bundleContext;

    private final Logger log = LoggerFactory.getLogger(getClass());

    public CoppaSiteProvider(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public List<Site> getSites(List<String> assignedIdentifiers) {
        List<Site> sites = new ArrayList<Site>();

        for (Id id: createIds(assignedIdentifiers)) {
            Organization o = CoppaProviderHelper.getCoppaAccessor(bundleContext).getOrganization(id);
            sites.add(o == null ? null : createSite(o));
        }

        return sites;
    }

    public List<Site> search(String partialName) {
        Organization example = createNameExample(partialName);

        Organization[] raw = CoppaProviderHelper.getCoppaAccessor(bundleContext).searchOrganizations(example);
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
        return CoppaProviderConstants.PROVIDER_TOKEN;
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

    private Id[] createIds(List<String> assignedidentifiers) {
        List<Id> iis = new ArrayList<Id>();
        for(String ai : assignedidentifiers) {
            iis.add(fromAssignedIdentifier(ai).createId());
        }
        return iis.toArray(new Id[0]);
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
