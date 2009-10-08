package edu.northwestern.bioinformatics.studycalendar.dataproviders.coppa.direct.helpers;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.coppa.po.Organization;

import java.util.ArrayList;
import java.util.List;

public class CoppaProviderHelper {
    public static Site pscSite(Organization organization) {
        Site site = new Site();
        site.setName(organization.getName().getPart().get(0).getValue());
        site.setAssignedIdentifier(organization.getIdentifier().getExtension());
        return site;
    }

    public static List<Site> pscSites(Organization... organizations) {
        List<Site> sites = new ArrayList<Site>();
        for (Organization org : organizations) {
            sites.add(pscSite(org));
        }
        return sites;
    }
}