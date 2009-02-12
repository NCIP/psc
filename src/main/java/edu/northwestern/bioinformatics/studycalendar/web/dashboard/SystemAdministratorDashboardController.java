package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * Subclass to attach access control
 *
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class SystemAdministratorDashboardController extends org.springframework.web.servlet.mvc.ParameterizableViewController {
    public SystemAdministratorDashboardController() {
        setViewName("redirectToAdministration");
    }
}
