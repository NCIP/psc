package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * Subclass to attach access control
 *
 * @author Rhett Sutphin
 */
@AccessControl(roles = Role.STUDY_ADMIN)
public class StudyAdministratorDashboardController extends org.springframework.web.servlet.mvc.ParameterizableViewController {
    public StudyAdministratorDashboardController() {
        setViewName("redirectToStudyList");
    }
}
