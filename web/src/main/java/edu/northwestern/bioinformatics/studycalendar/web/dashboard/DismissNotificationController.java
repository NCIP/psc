/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.dashboard;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.NotificationDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Notification;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import edu.northwestern.bioinformatics.studycalendar.web.LiteralTextView;
import edu.northwestern.bioinformatics.studycalendar.web.PscAbstractController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.Map;

/**
 * TODO: this functionality belongs in the API.
 *
 * @author Rhett Sutphin
 */
public class DismissNotificationController
    extends PscAbstractController
    implements PscAuthorizedHandler
{
    private NotificationDao notificationDao;
    private ApplicationSecurityManager applicationSecurityManager;

    public Collection<ResourceAuthorization> authorizations(
        String httpMethod, Map<String, String[]> queryParameters
    ) throws Exception {
        return ResourceAuthorization.createCollection(PscRole.STUDY_SUBJECT_CALENDAR_MANAGER);
    }

    @Override
    protected ModelAndView handleRequestInternal(
        HttpServletRequest request, HttpServletResponse response
    ) throws Exception {
        if (!request.getMethod().equals("POST")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "POST only");
            return null;
        }

        Notification notification = notificationDao.getById(
            ServletRequestUtils.getRequiredIntParameter(request, "notification"));
        if (notification == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

        UserStudySubjectAssignmentRelationship rel = new UserStudySubjectAssignmentRelationship(
            applicationSecurityManager.getUser(), notification.getAssignment());
        if (!rel.getCanUpdateSchedule()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You can't modify this schedule");
            return null;
        }

        notification.setDismissed(true);
        notificationDao.save(notification);

        return new ModelAndView(new LiteralTextView("Dismissed"));
    }

    /////// CONFIGURATION

    @Required
    public void setNotificationDao(NotificationDao notificationDao) {
        this.notificationDao = notificationDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
