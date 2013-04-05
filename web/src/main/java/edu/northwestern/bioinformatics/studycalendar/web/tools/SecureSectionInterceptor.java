/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ControllerRequiredAuthorityExtractor;
import gov.nih.nci.cabig.ctms.web.chrome.Section;
import gov.nih.nci.cabig.ctms.web.chrome.SectionInterceptor;
import gov.nih.nci.cabig.ctms.web.chrome.Task;
import org.acegisecurity.GrantedAuthority;
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
    private ConfigurableListableBeanFactory beanFactory;
    private ControllerRequiredAuthorityExtractor controllerRequiredAuthorityExtractor;
    private ApplicationSecurityManager applicationSecurityManager;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        super.preHandle(request,  response, handler);

        PscUser user = applicationSecurityManager.getUser();

        List<Section> filtered = new ArrayList<Section>();

        for (Section section : getSections()) {
            Set<GrantedAuthority> allowed = new HashSet<GrantedAuthority>();
            for (Task task : section.getTasks()) {
                Controller controller = (Controller) beanFactory.getBean(task.getLinkName(), Controller.class);
                allowed.addAll(Arrays.asList(
                    controllerRequiredAuthorityExtractor.
                        getAllowedAuthoritiesForController(controller)));
            }

            for (GrantedAuthority role : allowed) {
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

    ////// CONFIGURATION

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }

    @Required
    public void setControllerRequiredAuthorityExtractor(
        ControllerRequiredAuthorityExtractor controllerRequiredAuthorityExtractor
    ) {
        this.controllerRequiredAuthorityExtractor = controllerRequiredAuthorityExtractor;
    }
}
