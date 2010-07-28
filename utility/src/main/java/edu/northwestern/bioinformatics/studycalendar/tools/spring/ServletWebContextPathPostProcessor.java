package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * A post-processor which injects the application context path into beans which
 * implement {@link edu.northwestern.bioinformatics.studycalendar.tools.spring.WebContextPathAware}.
 * The value to use is taken from the {@link javax.servlet.ServletContext}.
 *
 * @author Rhett Sutphin
 */
public class ServletWebContextPathPostProcessor implements BeanFactoryPostProcessor, ServletContextAware {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private ServletContext servletContext;

    @SuppressWarnings({ "unchecked" })
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        String path = servletContext.getContextPath();
        Map<String, WebContextPathAware> awareBeans =
            (Map<String, WebContextPathAware>) BeanFactoryUtils.
                beansOfTypeIncludingAncestors(beanFactory, WebContextPathAware.class);
        log.debug("Setting web context path \"{}\" on {} bean(s)", path, awareBeans.size());
        for (Map.Entry<String, WebContextPathAware> entry : awareBeans.entrySet()) {
            log.trace(" - setting on bean {}", entry.getKey());
            entry.getValue().setWebContextPath(path);
        }
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
