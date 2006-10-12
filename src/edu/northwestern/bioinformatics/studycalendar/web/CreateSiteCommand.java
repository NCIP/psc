package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;


public class CreateSiteCommand {
    private String name;

    private SiteService siteService;

    public Site createSite() {
    	Site site = new Site();
    	site.setName(name);
        return siteService.createSite(site);
    }

    ////// CONFIGURATION

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    
    ////// BOUND PROPERTIES

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
