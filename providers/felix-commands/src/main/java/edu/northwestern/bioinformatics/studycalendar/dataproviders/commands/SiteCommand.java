package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import org.apache.felix.shell.Command;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SiteCommand implements Command {
    private BundleContext bundleContext;

    public SiteCommand(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public String getName() {
        return "site";
    }

    public String getUsage() {
        return "site (get|search) arg";
    }

    public String getShortDescription() {
        return "executes site provider methods";
    }

    public void execute(String command, PrintStream out, PrintStream err) {
        String[] parts = command.split("\\s+", 3);
        String subcommand = parts.length > 1 ? parts[1] : null;
        String arg = parts.length > 2 ? parts[2] : null;
        if ("get".equals(subcommand)) {
            doGet(arg, out, err);
        } else if ("search".equals(subcommand)) {
            doSearch(arg, out, err);
        } else {
            err.println("Please specify a valid subcommand (get or search)");
        }
    }

    private void doGet(String arg, PrintStream out, PrintStream err) {
        if (arg == null) { err.println("Please specify an argument for get"); return; }

        for (Map.Entry<String, SiteProvider> provider : getSiteProviders().entrySet()) {
            out.println(provider.getKey());
            Site match = provider.getValue().getSite(arg);
            if (match == null) {
                out.println("  No match");
            } else {
                printSite(match, out);
            }
        }
    }

    private void doSearch(String arg, PrintStream out, PrintStream err) {
        if (arg == null) { err.println("Please specify an argument for search"); return; }

        for (Map.Entry<String, SiteProvider> provider : getSiteProviders().entrySet()) {
            out.println(provider.getKey());
            Collection<Site> matches = provider.getValue().search(arg);
            if (matches == null || matches.isEmpty()) {
                out.println("  No matches");
            } else {
                for (Site site : matches) {
                    printSite(site, out);
                }
            }
        }
    }

    private void printSite(Site match, PrintStream out) {
        out.println(String.format("- Site id=%s name=%s",
            match.getAssignedIdentifier(), match.getName()));
    }

    private Map<String, SiteProvider> getSiteProviders() {
        Map<String, SiteProvider> providers = new LinkedHashMap<String, SiteProvider>();
        ServiceReference[] providerRefs = getProviderReferences();
        for (ServiceReference ref : providerRefs) {
            SiteProvider provider = (SiteProvider) bundleContext.getService(ref);
            providers.put(ref.getBundle().getSymbolicName(), provider);
        }
        return providers;
    }

    private ServiceReference[] getProviderReferences() {
        try {
            return bundleContext.getAllServiceReferences(SiteProvider.class.getName(), null);
        } catch (InvalidSyntaxException e) {
            throw new Error("This shouldn't be possible", e);
        }
    }
}
