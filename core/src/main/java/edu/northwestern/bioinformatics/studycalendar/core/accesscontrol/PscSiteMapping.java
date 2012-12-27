/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.BasePscSiteMapping;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class PscSiteMapping extends BasePscSiteMapping {
    private SiteDao siteDao;

    @Override
    public List<Site> getApplicationInstances(List<String> identifiers) {
        return siteDao.getByAssignedIdentifiers(identifiers);
    }

    ////// CONFIGURATION

    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }
}
