/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.console;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.EmbedderFilesystemConfiguration;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.FrameworkFactoryFinder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import org.osgi.framework.BundleContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;

/**
 * @author Rhett Sutphin
 */
public class EmbedderConsole {
    public static void main(String[] args) {
        String embedderRoot = args[0];

        BundleContext bundleContext = launch(embedderRoot);
        if (bundleContext == null) {
            System.exit(1);
        }

        loadCoreApplicationContext(bundleContext);
    }

    private static BundleContext launch(String embedderRoot) {
        Embedder embedder = new Embedder();
        embedder.setFrameworkFactory(FrameworkFactoryFinder.getFrameworkFactory());
        embedder.setConfiguration(new EmbedderFilesystemConfiguration(new File(embedderRoot)));
        return embedder.start();
    }

    private static void loadCoreApplicationContext(BundleContext bundleContext) {
        System.out.println("Loading core application context");
        ApplicationContext withBundleContext = ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().
                put("bundleContext", bundleContext).
                toMap());
        ClassPathXmlApplicationContext all = new ClassPathXmlApplicationContext(withBundleContext);
        all.setConfigLocations(StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS);
        all.refresh();
    }
}
