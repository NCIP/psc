package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class ListUsersController extends PscSimpleFormController {
    private UserService userService;

    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        List<User> users = userService.getAllUsers();
        model.put("users", users);

        return new ModelAndView("listUsers", model);
    }

    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
