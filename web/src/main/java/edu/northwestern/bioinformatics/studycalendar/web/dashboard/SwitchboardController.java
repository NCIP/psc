/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRoleUse;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SwitchboardController extends PscAbstractController implements PscAuthorizedHandler {
    private ApplicationSecurityManager applicationSecurityManager;

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ALL_AUTHORIZED;
    }

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        PscUser user = applicationSecurityManager.getUser();
        if (user.hasRole(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER)) {
            return new ModelAndView("redirectToDashboard");
        }
        for (PscRole role : PscRole.valuesWithStudyAccess()) {
            if (user.hasRole(role)) return new ModelAndView("redirectToStudyList");
        }
        if (user.hasRole(PscRole.BUSINESS_ADMINISTRATOR)) {
            return new ModelAndView("redirectToActivities");
        }
        for (PscRole role : PscRoleUse.ADMINISTRATION.roles()) {
            if (user.hasRole(role)) return new ModelAndView("redirectToAdministration");
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN,
            String.format("None of your roles (%s) permit access to the PSC UI",
                Arrays.asList(user.getAuthorities())));
        return null;
    }

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
