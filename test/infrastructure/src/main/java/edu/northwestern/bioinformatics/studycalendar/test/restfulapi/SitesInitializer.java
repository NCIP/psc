/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.service.SiteService;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.ConnectionSource;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import org.jvyaml.YAML;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SitesInitializer extends RowPreservingInitializer implements InitializingBean {
    private SiteService siteService;
    private SiteDao siteDao;

    private Resource yaml;
    private Collection<Map<String, String>> siteData;

    public SitesInitializer() {
        super("sites");
    }

    @Transactional
    public void oneTimeSetup(ConnectionSource connectionSource) {
        super.oneTimeSetup(connectionSource);
        for (Map<String, String> siteAttrs : siteData) {
            createOrUpdateSite(siteAttrs);
        }
    }

    private void createOrUpdateSite(Map<String, String> attrs) {
        Site site = siteDao.getByAssignedIdentifier(attrs.get("assignedIdentifier"));
        if (site == null) site = new Site();
        BeanWrapper wrappedSite = new BeanWrapperImpl(site);
        for (Map.Entry<String, String> pair : attrs.entrySet()) {
            wrappedSite.setPropertyValue(pair.getKey(), pair.getValue());
        }
        siteService.createOrUpdateSite(site);
    }

    ////// CONFIGURATION

    @Required
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setYamlResource(Resource yamlResource) {
        this.yaml = yamlResource;
    }

    @SuppressWarnings({ "unchecked" })
    public void afterPropertiesSet() throws Exception {
        Map<String, Map<String, String>> sitesMap = (Map<String, Map<String, String>>) YAML.load(new InputStreamReader(yaml.getInputStream()));
        siteData = sitesMap == null ? null : sitesMap.values();
    }
}
