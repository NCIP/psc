package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;

import java.util.Collection;
import java.util.Map;
/**
 * Subclass to attach access control
 *
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_ADMIN)
public class StudyAdministratorDashboardController extends org.springframework.web.servlet.mvc.ParameterizableViewController
                                                   implements PscAuthorizedHandler {
    public StudyAdministratorDashboardController() {
        setViewName("redirectToStudyList");
    }
    
    public Collection<ResourceAuthorization> authorizations(String httpMethod, Map<String, String[]> queryParameters) {
        return NONE_AUTHORIZED;
    }
}
