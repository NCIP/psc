package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.servlet.mvc.Controller;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ControllerProtectionElementCreator implements BeanFactoryPostProcessor {
    private static Log log = LogFactory.getLog(ControllerProtectionElementCreator.class);

    private String urlPrefix;
    private StudyCalendarAuthorizationManager studyCalendarAuthorizationManager;

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            ProtectableController controller = new ProtectableController(controllerName, beanFactory);
            List<String> groupNames = controller.getRequiredProtectionGroupNames();
            if (groupNames != null) {
                studyCalendarAuthorizationManager.registerUrl(controller.getUrl(), groupNames);
            }
        }
    }

    private String getUrlPrefix() {
        return urlPrefix == null ? "" : urlPrefix;
    }

    ////// CONFIGURATION

    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager studyCalendarAuthorizationManager) {
        this.studyCalendarAuthorizationManager = studyCalendarAuthorizationManager;
    }

    public void setUrlPrefix(String urlPrefix) {
        this.urlPrefix = urlPrefix;
    }

    ////// COMMAND OBJS

    private class ProtectableController {
        private String beanName;
        private ConfigurableListableBeanFactory beanFactory;

        public ProtectableController(String beanName, ConfigurableListableBeanFactory beanFactory) {
            this.beanName = beanName;
            this.beanFactory = beanFactory;
        }

        /**
         * Uses this first alias for this bean that starts with '/'.   This is based on the behavior
         * of {@link org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping}.
         */
        public String getUrl() {
            String[] aliases = beanFactory.getAliases(beanName);
            for (String alias : aliases) {
                if (alias.startsWith("/")) return getUrlPrefix() + alias;
            }
            throw new StudyCalendarSystemException(
                "The controller bean " + beanName + " does not have a URL mapping");
        }

        public Class<? extends Controller> getControllerClass() {
            String className = getBeanDefinition().getBeanClassName();
            try {
                return (Class<? extends Controller>) Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new StudyCalendarSystemException("Class for controller bean " + beanName + " not found", e);
            }
        }

        public List<String> getRequiredProtectionGroupNames() {
            AccessControl ac = getControllerClass().getAnnotation(AccessControl.class);
            if (ac == null) {
                log.warn("No AccessControl annotations on " + getControllerClass().getName() + ".  CSM will not be automatically configured for this controller.");
                return null;
            }
            List<String> pgNames = new ArrayList<String>(ac.protectionGroups().length);
            for (StudyCalendarProtectionGroup protectionGroup : ac.protectionGroups()) {
                pgNames.add(protectionGroup.csmName());
            }
            return pgNames;
        }

        private BeanDefinition getBeanDefinition() {
            return beanFactory.getBeanDefinition(beanName);
        }
    }
}
