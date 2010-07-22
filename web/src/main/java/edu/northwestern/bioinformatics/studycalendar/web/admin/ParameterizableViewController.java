package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class ParameterizableViewController extends org.springframework.web.servlet.mvc.ParameterizableViewController
                                           implements CrumbSource, PscAuthorizedHandler {
    private Crumb crumb;

    ////// IMPLEMENTATION OF CrumbSource

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(SYSTEM_ADMINISTRATOR);
    }

    public Crumb getCrumb() {
        return crumb;
    }

    ////// CONFIGURATION

    public void setCrumb(Crumb crumb) {
        this.crumb = crumb;
    }
}
