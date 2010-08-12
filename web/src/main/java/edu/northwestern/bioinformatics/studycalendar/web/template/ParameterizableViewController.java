package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.Map;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.STUDY_CREATOR;

/**
 * Need a subclass here solely to apply the access control annotation.
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ParameterizableViewController extends org.springframework.web.servlet.mvc.ParameterizableViewController
        implements PscAuthorizedHandler {

    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return ResourceAuthorization.createCollection(STUDY_CALENDAR_TEMPLATE_BUILDER, STUDY_CREATOR );
    }
}
