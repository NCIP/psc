/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.tools.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;

import javax.servlet.ServletContext;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class ConcreteStaticApplicationContext extends GenericApplicationContext {
    private static final Logger log = LoggerFactory.getLogger(ConcreteStaticApplicationContext.class);

    private ConcreteStaticApplicationContext(DefaultListableBeanFactory bf) {
        super(bf);
    }

    public static ApplicationContext create(Map<String, Object> beans) {
        log.debug("Creating concrete application context with {} bean(s)", beans.size());
        log.trace("- Specifically {}", beans);
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        for (Map.Entry<String, Object> entry : beans.entrySet()) {
            factory.addBean(entry.getKey(), entry.getValue());
        }
        ConcreteStaticApplicationContext context = new ConcreteStaticApplicationContext(
            new DefaultListableBeanFactory(factory));
        context.refresh();
        return context;
    }

    public static WebApplicationContext createWebApplicationContext(
        Map<String, Object> beans, ServletContext servletContext
    ) {
        StaticWebApplicationContext ctx = new StaticWebApplicationContext();
        ctx.setServletContext(servletContext);
        ctx.setParent(create(beans));
        ctx.refresh();
        return ctx;
    }
}
