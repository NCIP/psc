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
 * A post-processor which injects the application context path into beans which
 * implement {@link edu.northwestern.bioinformatics.studycalendar.tools.spring.WebContextPathAware}.
 * The context path is injected on the first request, using {@link ServletWebContextPathAwareFilter}
 *
 * @author Rhett Sutphin
 */
public class ServletWebContextPathPostProcessor implements BeanFactoryPostProcessor {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, WebContextPathAware> awareBeans;
    private volatile boolean registered = false;

    @SuppressWarnings({ "unchecked" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        awareBeans = (Map<String, WebContextPathAware>) BeanFactoryUtils.
            beansOfTypeIncludingAncestors(beanFactory, WebContextPathAware.class);
    }

    public void registerRequest(HttpServletRequest request) {
        if (registered) return; // bypass acquiring the lock if it definitely isn't needed
        registerRequestInternal(request);
    }

    private synchronized void registerRequestInternal(HttpServletRequest request) {
        if (registered) return; // repeat since the outer one is deliberately unsynchronized

        String path = request.getContextPath();
        log.debug("Setting web context path \"{}\" on {} bean(s)", path, awareBeans.size());
        for (Map.Entry<String, WebContextPathAware> entry : awareBeans.entrySet()) {
            log.trace(" - setting on bean {}", entry.getKey());
            entry.getValue().setWebContextPath(path);
        }

        registered = true;
        awareBeans = null; // memory paranoia
    }
}
