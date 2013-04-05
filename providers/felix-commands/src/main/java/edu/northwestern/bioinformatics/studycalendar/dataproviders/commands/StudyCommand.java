/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySecondaryIdentifier;
import org.osgi.framework.BundleContext;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import static java.lang.String.*;

/**
 * @author Rhett Sutphin
 */
public class StudyCommand extends ProviderCommand<StudyProvider> {
    public StudyCommand(BundleContext bundleContext) {
        super(bundleContext);
        addSubcommand(new SearchSubcommand());
    }

    @Override
    protected Class<StudyProvider> providerClass() {
        return StudyProvider.class;
    }

    @Override
    public String getName() {
        return "study";
    }

    @Override
    public String getUsage() {
        return "study search <arg>";
    }

    @Override
    public String getShortDescription() {
        return "execute study provider methods.";
    }

    private void printStudy(Study study, PrintStream out) {
        out.println(format("- Study assigned=%s", study.getAssignedIdentifier()));
        out.println(format("  longTitle=%s", study.getLongTitle()));
        out.println("  secondary identifiers=[");
        int maxTypeLength = 0;
        for (StudySecondaryIdentifier ssi : study.getSecondaryIdentifiers()) {
            maxTypeLength = Math.max(maxTypeLength, ssi.getType().length());
        }
        for (StudySecondaryIdentifier identifier : study.getSecondaryIdentifiers()) {
            out.println(format("    type=%-" + maxTypeLength + "s value=%s", identifier.getType(), identifier.getValue()));
        }
        out.println("  ]");
    }

    private class SearchSubcommand implements Subcommand {
        public String getName() {
            return "search";
        }

        public void execute(String arg, PrintStream out, PrintStream err) {
            for (Map.Entry<String, StudyProvider> provider : getProviders(err).entrySet()) {
                out.println(provider.getKey());
                Collection<Study> matches = provider.getValue().search(arg);
                if (matches == null || matches.isEmpty()) {
                    out.println("  No matches");
                } else {
                    for (Study study : matches) {
                        printStudy(study, out);
                    }
                }
            }
        }
    }
}
