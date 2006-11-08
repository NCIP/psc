package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.utils.spring.ControllerUrlResolver;
import edu.northwestern.bioinformatics.studycalendar.utils.spring.ResolvedControllerReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.mvc.Controller;
import org.springframework.core.Ordered;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class ControllerProtectionElementCreator implements BeanFactoryPostProcessor, Ordered {
    private static Log log = LogFactory.getLog(ControllerProtectionElementCreator.class);

    private ControllerUrlResolver urlResolver;
    private StudyCalendarAuthorizationManager studyCalendarAuthorizationManager;

    // Must occur after BeanNameControllerUrlResolver
    public int getOrder() { return 3; }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String[] controllerNames = beanFactory.getBeanNamesForType(Controller.class, false, false);
        for (String controllerName : controllerNames) {
            ResolvedControllerReference controller = urlResolver.resolve(controllerName);
            List<String> groupNames = getRequiredProtectionGroupNames(controller);
            if (groupNames != null) {
                studyCalendarAuthorizationManager.registerUrl(controller.getUrl(), groupNames);
            }
        }
    }

    private List<String> getRequiredProtectionGroupNames(ResolvedControllerReference controller) {
        Class<? extends Controller> clazz = controller.getControllerClass();
        AccessControl ac = clazz.getAnnotation(AccessControl.class);
        if (ac == null) {
            log.warn("No AccessControl annotations on " + clazz.getName() + ".  CSM will not be automatically configured for this controller.");
            return null;
        }
        List<String> pgNames = new ArrayList<String>(ac.protectionGroups().length);
        for (StudyCalendarProtectionGroup protectionGroup : ac.protectionGroups()) {
            pgNames.add(protectionGroup.csmName());
        }
        return pgNames;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyCalendarAuthorizationManager(StudyCalendarAuthorizationManager studyCalendarAuthorizationManager) {
        this.studyCalendarAuthorizationManager = studyCalendarAuthorizationManager;
    }

    @Required
    public void setUrlResolver(ControllerUrlResolver urlResolver) {
        this.urlResolver = urlResolver;
    }
}
