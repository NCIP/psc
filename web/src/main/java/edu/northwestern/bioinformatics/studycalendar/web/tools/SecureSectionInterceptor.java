package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.SectionInterceptor;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptor extends SectionInterceptor implements BeanFactoryPostProcessor {
    private UserDao userDao;
    private ConfigurableListableBeanFactory beanFactory;
    private ApplicationSecurityManager applicationSecurityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.preHandle(request,  response, handler);

        String userName = applicationSecurityManager.getUserName();
        if (userName == null) {
            // This shouldn't be possible
            throw new StudyCalendarSystemException("No user available from security context");
        }
        User user = userDao.getByName(userName);
        if (user == null) {
            // Neither should this be possible
            throw new StudyCalendarSystemException("%s in security context does not map to an actual user", userName);
        }

        List<Section> filtered = new ArrayList<Section>();

        for (Section section : getSections()) {
            Set<Role> allowed = new HashSet<Role>();
            for (Task task : section.getTasks()) {
                Controller controller = (Controller) beanFactory.getBean(task.getLinkName(), Controller.class);
                allowed.addAll(getAllowedRoles(controller));
            }

            for (Role role : allowed) {
                if (user.hasRole(role) && !filtered.contains(section)) {
                    filtered.add(section);
                }
            }
        }

        request.setAttribute(prefix("sections"), filtered);

        return true;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    protected List<Role> getAllowedRoles(Controller controller) {
        Class<? extends Controller> clazz = controller.getClass();
        AccessControl ac = clazz.getAnnotation(AccessControl.class);
        Role[] roles = Role.values();
        if (ac != null) {
            roles = ac.roles();
        }
        return Arrays.asList(roles);
    }

    ////// CONFIGURATION

    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
