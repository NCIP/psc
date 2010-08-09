package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import java.util.Collection;
import java.util.Map;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

@AccessControl(roles =Role.SYSTEM_ADMINISTRATOR)
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
