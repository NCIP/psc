package edu.northwestern.bioinformatics.studycalendar.core;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.mocks.osgi.PscTestingBundleContext;
import edu.northwestern.bioinformatics.studycalendar.osgi.hostservices.Activator;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import static edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarTestCoreApplicationContextBuilder {
    private static String[] SUPPLEMENTAL_CONFIG_LOCATIONS = {
        "classpath:applicationContext-core-testing-osgi.xml"
    };

    private static final StaticApplicationContextHelper helper = new StaticApplicationContextHelper() {
        @Override
        protected ApplicationContext createApplicationContext() {
            return StudyCalendarTestCoreApplicationContextBuilder.createApplicationContext();
        }
    };

    public static ApplicationContext createApplicationContext() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(
            createBundleContextApplicationContext());
        context.setConfigLocations(DEPLOYED_CONFIG_LOCATIONS);
        context.refresh();
        System.out.println("Bundle context? " + context.getBean("bundleContext"));
        return context;
    }

    private static ApplicationContext createBundleContextApplicationContext() {
        PscTestingBundleContext bundleContext = new PscTestingBundleContext();
        ApplicationContext bundleContextContext = ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().put("bundleContext", bundleContext).toMap());
        injectHostServices(bundleContext);
        return bundleContextContext;
    }

    private static void injectHostServices(BundleContext bundleContext) {
        try {
            new Activator().start(bundleContext);
        } catch (Exception e) {
            throw new StudyCalendarSystemException("Activating host services failed", e);
        }
    }

    private static String[] allLocations() {
        int coreCount = DEPLOYED_CONFIG_LOCATIONS.length;
        String[] loc = new String[coreCount + SUPPLEMENTAL_CONFIG_LOCATIONS.length];
        System.arraycopy(
            DEPLOYED_CONFIG_LOCATIONS,     0, loc, 0,         coreCount);
        System.arraycopy(
            SUPPLEMENTAL_CONFIG_LOCATIONS, 0, loc, coreCount, SUPPLEMENTAL_CONFIG_LOCATIONS.length);
        return loc;
    }

    public static ApplicationContext getApplicationContext() {
        return helper.getApplicationContext();
    }
}
