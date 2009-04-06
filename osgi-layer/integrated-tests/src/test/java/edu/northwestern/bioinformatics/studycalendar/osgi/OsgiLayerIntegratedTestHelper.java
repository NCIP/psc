package edu.northwestern.bioinformatics.studycalendar.osgi;

import org.dynamicjava.osgi.da_launcher.Launcher;
import org.dynamicjava.osgi.da_launcher.LauncherFactory;
import org.dynamicjava.osgi.da_launcher.LauncherSettings;
import org.dynamicjava.osgi.da_launcher.web.DaLauncherWebConstants;
import org.osgi.framework.BundleContext;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerIntegratedTestHelper {
    private static BundleContext bundleContext;

    public static void registerBundleContext(ServletContext servletContext) throws IOException {
        servletContext.setAttribute(DaLauncherWebConstants.ServletContextAttributes.BUNDLE_CONTEXT_KEY, getBundleContext());
    }

    public static synchronized BundleContext getBundleContext() throws IOException {
        if (bundleContext == null) {
            LauncherSettings settings = new LauncherSettings(
                getModuleRelativeDirectory("osgi-layer", "target/test/da-launcher").getAbsolutePath());
            Launcher launcher = new LauncherFactory(settings).createLauncher();
            launcher.launch();
            bundleContext = launcher.getOsgiFramework().getBundleContext();
        }
        return bundleContext;
    }

    public static File getModuleRelativeDirectory(String moduleName, String directory) throws IOException {
        File dir = new File(directory);
        if (dir.exists()) return dir;

        dir = new File(moduleName.replaceAll(":", "/"), directory);
        if (dir.exists()) return dir;

        throw new FileNotFoundException(
            String.format("Could not find directory %s relative to module %s from current directory %s",
                directory, moduleName, new File(".").getCanonicalPath()));
    }
}
