package edu.northwestern.bioinformatics.studycalendar.osgi.sysprop;

import org.apache.commons.lang.StringEscapeUtils;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class Activator implements BundleActivator {
    @SuppressWarnings({ "unchecked" })
    public void start(BundleContext bundleContext) throws Exception {
        System.out.println("Dumping OSGi-visible system properties for PSC");
        System.out.println("==============================================");
        System.out.println("Dumping from " + bundleContext.getBundle().getLocation());
        System.out.println("----------------------------------------------");
        List<String> propertyNames = new ArrayList<String>((Collection) System.getProperties().keySet());
        Collections.sort(propertyNames);
        for (String name : propertyNames) {
            System.out.println(String.format("%s=\"%s\"",
                name, StringEscapeUtils.escapeJava(System.getProperty(name))));
        }
        System.out.println("==============================================");
    }

    public void stop(BundleContext bundleContext) throws Exception {
    }
}
