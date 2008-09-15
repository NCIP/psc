package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NewSiteCommand {
    private String name;
    private String assignedIdentifier;
    private SiteService siteService;
    private Site site;
   
    public NewSiteCommand(Site site, SiteService siteService) {

        this.site = site;
        this.siteService = siteService;
        if (this.siteService == null) throw new IllegalArgumentException("siteService required");
     }

    public Site createSite() throws Exception {
        site.setName(name);
        site.setAssignedIdentifier(assignedIdentifier);
        return siteService.createOrUpdateSite(site);
    }



    ////// BOUND PROPERTIES

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public Site getSite() {
        return site;
    }
     public String getAssignedIdentifier() {
        return assignedIdentifier;
    }

    public void setAssignedIdentifier(String assignedIdentifier) {
        this.assignedIdentifier = assignedIdentifier;
    }


}
