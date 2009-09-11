package edu.northwestern.bioinformatics.studycalendar.dataproviders.mock;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class MockSiteProvider implements SiteProvider {
    private Map<String, String> sites;

    public Site getSite(String assignedIdentifier) {
        return createSite(assignedIdentifier);
    }

    public List<Site> search(String partialName) {
        List<Site> result = new LinkedList<Site>();
        for (Map.Entry<String, String> entry : sites.entrySet()) {
            if (entry.getValue().toLowerCase().contains(partialName.toLowerCase())) {
                result.add(createSite(entry.getKey()));
            }
        }
        return result;
    }

    public String providerToken() {
        return "mock - NOT FOR PRODUCTION";
    }

    private Site createSite(String assignedIdentifier) {
        if (sites.get(assignedIdentifier) != null) {
            Site newSite = new Site();
            newSite.setAssignedIdentifier(assignedIdentifier);
            newSite.setName(sites.get(assignedIdentifier));
            return newSite;
        } else {
            return null;
        }
    }

    public void setSites(Map<String, String> sites) {
        this.sites = sites;
    }
}
