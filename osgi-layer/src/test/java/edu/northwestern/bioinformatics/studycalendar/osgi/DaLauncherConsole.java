package edu.northwestern.bioinformatics.studycalendar.osgi;

import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class DaLauncherConsole {
    private static final Collection<String> TELNET_BUNDLES = Arrays.asList(
        "org.knopflerfish.bundle.consoletelnet-IMPL"
    );

    public static void main(String[] args) {
        String daLauncherHome = args[0];

        LauncherSettings settings = new LauncherSettings(daLauncherHome);
        Launcher launcher = new LauncherFactory(settings).createLauncher();
        launcher.launch();

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
        } else {
            System.out.println("Telnet console now available.  Press ^C to end.");
        }
    }
}
