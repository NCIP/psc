/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.nwu.bioinformatics.commons.spring.Validatable;
import org.springframework.validation.Errors;
import org.apache.commons.lang.StringUtils;

public class NewSiteCommand implements Validatable {
    private SiteService siteService;
    private Site site;
   
    public NewSiteCommand(Site site, SiteService siteService) {
        this.site = site;
        this.siteService = siteService;
        if (this.siteService == null) throw new IllegalArgumentException("siteService required");
     }

    public Site createSite() throws Exception {
        if (site.getAssignedIdentifier() == null || StringUtils.isEmpty(site.getAssignedIdentifier())) {
            site.setAssignedIdentifier(site.getName());
        }
        return siteService.createOrUpdateSite(site);
    }

    ////// BOUND PROPERTIES

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public Site getSite() {
        return site;
    }

    public void setSite(Site site) {
        this.site = site;
    }


    public void validate(Errors errors) {
        if (site != null) {
            if (site.getName() == null || StringUtils.isEmpty(site.getName())) {
                errors.rejectValue("site.name", "error.site.name.is.empty");
            }
            else  if (site.getId() == null) {
                if (siteService.getByName(site.getName()) != null) {
                    errors.rejectValue("site.name", "error.site.name.already.exists");
                } else if (siteService.getByAssignedIdentifier(site.getAssignedIdentifier()) != null) {
                    errors.rejectValue("site.assignedIdentifier", "error.site.assignedIdentifier.already.exists");
                }
            }
        }
    }
}
