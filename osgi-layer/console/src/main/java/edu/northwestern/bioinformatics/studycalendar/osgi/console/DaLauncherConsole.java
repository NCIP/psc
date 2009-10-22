package edu.northwestern.bioinformatics.studycalendar.osgi.console;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarApplicationContextBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.internal.exceptions.LauncherException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class DaLauncherConsole {
    private static final Collection<String> TELNET_BUNDLES = Arrays.asList(
        "org.knopflerfish.bundle.consoletelnet-IMPL",
        "org.apache.felix.org.apache.felix.shell.remote"
    );

    public static void main(String[] args) {
        String daLauncherHome = args[0];

        Launcher launcher = launch(daLauncherHome);
        if (launcher == null) {
            System.exit(1);
        }

        loadCoreApplicationContext(launcher);

        findAndStartTelnetBundles(launcher);
    }

    private static Launcher launch(String daLauncherHome) {
        LauncherSettings settings = new LauncherSettings(daLauncherHome);
        Launcher launcher = new LauncherFactory(settings).createLauncher();
        try {
            launcher.launch();
        } catch (LauncherException le) {
            System.err.println("Launch interrupted by exception");
            le.printStackTrace();
            return null;
        }
        return launcher;
    }

    private static void loadCoreApplicationContext(Launcher launcher) {
        System.out.println("Loading core application context");
        ApplicationContext withBundleContext = ConcreteStaticApplicationContext.create(
            new MapBuilder<String, Object>().
                put("bundleContext", launcher.getOsgiFramework().getBundleContext()).
                toMap());
        ClassPathXmlApplicationContext all = new ClassPathXmlApplicationContext(withBundleContext);
        all.setConfigLocations(StudyCalendarApplicationContextBuilder.DEPLOYED_CONFIG_LOCATIONS);
        all.refresh();
    }

    private static void findAndStartTelnetBundles(Launcher launcher) {
        int startCount = 0;
        for (Bundle bundle : launcher.getOsgiFramework().getBundleContext().getBundles()) {
            if (TELNET_BUNDLES.contains(bundle.getSymbolicName())) {
                System.out.print("Starting " + bundle.getSymbolicName());
                try {
                    bundle.start();
                    System.out.println(" ... succeeded");
                    startCount++;
                } catch (BundleException e) {
                    System.out.println("...  FAILED");
                    e.printStackTrace(System.out);
                }
            }
        }
        if (startCount == 0) {
            System.out.println("No telnet bundles were startable.");
            System.exit(1);
        } else {
            System.out.println("Telnet console now available.  Press ^C to end.");
        }
    }
}
