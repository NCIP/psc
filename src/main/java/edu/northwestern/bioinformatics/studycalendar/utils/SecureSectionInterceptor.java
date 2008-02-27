package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.SectionInterceptor;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptor extends SectionInterceptor implements BeanFactoryPostProcessor {
    private UserDao userDao;
    private ControllerUrlResolver urlResolver;
    private ConfigurableListableBeanFactory beanFactory;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.preHandle(request,  response, handler);

        User user = userDao.getByName(ApplicationSecurityManager.getUser());

        List<Section> filtered = new ArrayList<Section>();

        for (Section section : getSections()) {
            Section newSection = copy(section);
            for (Task task : section.getTasks()) {
                Controller controller = (Controller) beanFactory.getBean(task.getLinkName(), Controller.class);
                List<Role> allowed = getAllowedRoles(controller);
                for (Role role : allowed) {
                    if (user.hasRole(role)) {
                        newSection.getTasks().add(task);
                    }
                }
            }
            filtered.add(newSection);
        }

        request.setAttribute("sections", filtered);
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

    protected Section copy(Section section) {
        Section newSection = new Section();
        newSection.setDisplayName(section.getDisplayName());
        newSection.setMainController(section.getMainController());
        newSection.setPathMappings(section.getPathMappings());
        newSection.setUrlResolver(urlResolver);
        return newSection;
    }

    ////// Bean Setters
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }
}
