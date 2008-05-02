package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.SectionInterceptor;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptor extends SectionInterceptor implements BeanFactoryPostProcessor {
    private UserDao userDao;
    private ConfigurableListableBeanFactory beanFactory;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.preHandle(request,  response, handler);

        User user = userDao.getByName(ApplicationSecurityManager.getUser());

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

    ////// Bean Setters
    public void setUserDao  (UserDao userDao) {
        this.userDao = userDao;
    }
}
