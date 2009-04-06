package edu.northwestern.bioinformatics.studycalendar.tools.spring;

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
    private ConcreteStaticApplicationContext(DefaultListableBeanFactory bf) {
        super(bf);
    }

    public static ApplicationContext create(Map<String, Object> beans) {
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
