/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

public class AdministrationController extends ParameterizableViewController implements CrumbSource, PscAuthorizedHandler {
    private DefaultCrumb crumb;

    public AdministrationController() {
        setViewName("administration");
        crumb = new DefaultCrumb("Admin");
    }

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(
            SYSTEM_ADMINISTRATOR, USER_ADMINISTRATOR, BUSINESS_ADMINISTRATOR, STUDY_QA_MANAGER,
            STUDY_TEAM_ADMINISTRATOR, PERSON_AND_ORGANIZATION_INFORMATION_MANAGER);
    }

    public Crumb getCrumb() {
        return crumb;
    }
}
