package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.SectionInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author John Dzak
 */
public class SecureSectionInterceptor extends SectionInterceptor implements BeanFactoryPostProcessor {
    private UserDao userDao;
    private ControllerUrlResolver urlResolver;
    private ConfigurableListableBeanFactory beanFactory;
    private Map controllerRolesMap;

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.preHandle(request,  response, handler);

        User user = userDao.getByName(ApplicationSecurityManager.getUser());

        List<Section> sections = new ArrayList<Section>();
        for (Section section : getSections()) {
            if (user.hasRole(Role.SITE_COORDINATOR)) {
                sections.add(section);
            }
        }

        //request.setAttribute(prefix("sections"), sections);
        request.setAttribute("sections", sections);
        return true;
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        controllerRolesMap = new HashMap<String, List<Role>>();
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
//        for (String controllerName : controllerNames) {
//            ResolvedControllerReference controller = urlResolver.resolve(controllerName);
//            ConfigAttributeDefinition groupNames = getRequiredProtectionGroupNames(controller);
//            if (groupNames != null) {
//                pathMap.addSecureUrl(new ApacheAntPattern(controller.getUrl(true)).toString(), groupNames);
//            }
//        }
//        filterInvocationInterceptor.setObjectDefinitionSource(pathMap);
    }

    public Map getControllerRolesMap() {
        return controllerRolesMap;
    }

    ////// Bean Setters
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }
}
