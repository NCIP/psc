/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import org.osgi.framework.BundleContext;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SiteCommand extends ProviderCommand<SiteProvider> {
    public SiteCommand(BundleContext bundleContext) {
        super(bundleContext);
        addSubcommand(new GetSubcommand());
        addSubcommand(new SearchSubcommand());
    }

    @Override
    protected Class<SiteProvider> providerClass() {
        return SiteProvider.class;
    }

    @Override
    public String getName() {
        return "site";
    }

    @Override
    public String getUsage() {
        return "site (get|search) <arg>";
    }

    @Override
    public String getShortDescription() {
        return "execute site provider methods.";
    }

    private void printSite(Site match, PrintStream out) {
        out.println(String.format("- Site id=%s name=%s",
            match.getAssignedIdentifier(), match.getName()));
    }

    private class GetSubcommand implements Subcommand {
        public String getName() {
            return "get";
        }

        public void execute(String arg, PrintStream out, PrintStream err) {
            for (Map.Entry<String, SiteProvider> provider : getProviders(err).entrySet()) {
                out.println(provider.getKey());
                Site match = provider.getValue().getSites(Arrays.asList(arg)).get(0);
                if (match == null) {
                    out.println("  No match");
                } else {
                    printSite(match, out);
                }
            }
        }
    }

    private class SearchSubcommand implements Subcommand {
        public String getName() {
            return "search";
        }

        public void execute(String arg, PrintStream out, PrintStream err) {
            for (Map.Entry<String, SiteProvider> provider : getProviders(err).entrySet()) {
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
    }
}
