/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * A post-processor which injects the web application context path into beans which
 * implement {@link edu.northwestern.bioinformatics.studycalendar.tools.spring.WebContextPathAware}
 * and application path into beans which implement 
 * {@link edu.northwestern.bioinformatics.studycalendar.tools.spring.ApplicationPathAware}.
 * The context path and application path are injected on the first request, using {@link ServletWebContextPathAwareFilter}
 *
 * @author Rhett Sutphin
 */
public class ServletWebContextPathPostProcessor implements BeanFactoryPostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, WebContextPathAware> webContextPathAwareBeans;
    private Map<String, ApplicationPathAware> applicationPathAwareBeans;
    private volatile boolean registered = false;

    @SuppressWarnings({ "unchecked" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        webContextPathAwareBeans = (Map<String, WebContextPathAware>) beanFactory.getBeansOfType(WebContextPathAware.class);
        applicationPathAwareBeans = (Map<String, ApplicationPathAware>) beanFactory.getBeansOfType(ApplicationPathAware.class);
    }

    public void registerRequest(HttpServletRequest request) {
        if (registered) return; // bypass acquiring the lock if it definitely isn't needed
        registerRequestInternal(request);
    }

    private synchronized void registerRequestInternal(HttpServletRequest request) {
        if (registered) return; // repeat since the outer one is deliberately unsynchronized

        String path = request.getContextPath();
        log.debug("Setting web context path \"{}\" on {} bean(s)", path, webContextPathAwareBeans.size());
        for (Map.Entry<String, WebContextPathAware> entry : webContextPathAwareBeans.entrySet()) {
            log.trace(" - setting on bean {}", entry.getKey());
            entry.getValue().setWebContextPath(path);
        }

        // For Application Path
        String applicationPath = request.getScheme().concat("://").concat(request.getServerName()).concat(":").
                concat(Integer.toString(request.getServerPort())).concat(path);
        log.debug("Setting application path \"{}\" on {} bean(s)", applicationPath, applicationPathAwareBeans.size());
        for (Map.Entry<String, ApplicationPathAware> entry : applicationPathAwareBeans.entrySet()) {
            log.trace(" - setting on bean {}", entry.getKey());
            entry.getValue().setApplicationPath(applicationPath);
        }
        
        registered = true;
        webContextPathAwareBeans = null; // memory paranoia
        applicationPathAwareBeans = null;
    }
}
