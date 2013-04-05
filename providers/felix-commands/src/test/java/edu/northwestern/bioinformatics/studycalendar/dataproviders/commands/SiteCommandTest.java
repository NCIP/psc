/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.SiteProvider;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import junit.framework.TestCase;
import static org.easymock.EasyMock.expect;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.mock.MockBundle;
import org.springframework.osgi.mock.MockServiceReference;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class SiteCommandTest extends TestCase {
    private SiteCommand command;

    private PrintStream out, err;
    private BundleContext bundleContext;
    private MockRegistry mockRegistry;
    private SiteProvider sp1;
    private SiteProvider sp2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRegistry = new MockRegistry();
        sp1 = mockRegistry.registerMockFor(SiteProvider.class);
        sp2 = mockRegistry.registerMockFor(SiteProvider.class);
        bundleContext = mockRegistry.registerMockFor(BundleContext.class);
        out = mockRegistry.registerMockFor(PrintStream.class);
        err = mockRegistry.registerMockFor(PrintStream.class);

        command = new SiteCommand(bundleContext);
    }

    public void testName() throws Exception {
        assertEquals("site", command.getName());
    }

    public void testShortDescription() throws Exception {
        assertEquals("execute site provider methods.", command.getShortDescription());
    }

    public void testUsage() throws Exception {
        assertEquals("site (get|search) <arg>", command.getUsage());
    }

    public void testCommandsExecuteAgainstAllFoundProviders() throws Exception {
        expectFoundProviders(sp1, sp2);
        expectGetOneSite(sp1, "fourteen", null);
        expectGetOneSite(sp2, "fourteen", Fixtures.createSite("A1", "fourteen"));
        expectOutput(
            "bundle0",
            "  No match",
            "bundle1",
            "- Site id=fourteen name=A1");

        doCommand("site get fourteen");
    }

    private void expectGetOneSite(SiteProvider provider, String ident, Site expected) {
        expect(provider.getSites(Arrays.asList(ident))).andReturn(Arrays.asList(expected));
    }

    public void testSearchWithResults() throws Exception {
        expectFoundProviders(sp1);
        expect(sp1.search("Center")).andReturn(Arrays.asList(
            Fixtures.createSite("RHL CCCenter", "IL036"),
            Fixtures.createSite("Mayo Center", "MN026")
        ));
        expectOutput("bundle0",
            "- Site id=IL036 name=RHL CCCenter",
            "- Site id=MN026 name=Mayo Center");
        doCommand("site search Center");
    }

    public void testSearchWithNoResults() throws Exception {
        expectFoundProviders(sp1);
        expect(sp1.search("Frazz")).andReturn(Collections.<Site>emptyList());
        expectOutput("bundle0", "  No matches");
        doCommand("site search Frazz");
    }

    public void testSearchWithNoProviders() throws Exception {
        expectFoundProviders();
        expectError("No site providers active");
        doCommand("site get elf");
    }

    public void testGetWithNoResults() throws Exception {
        expectFoundProviders(sp1);
        expectGetOneSite(sp1, "Frazz", null);
        expectOutput("bundle0", "  No match");
        doCommand("site get Frazz");
    }

    public void testErrorForNoCommand() throws Exception {
        expectError("Please specify a valid subcommand (get, search)");
        doCommand("site");
    }

    public void testErrorForUnknownCommand() throws Exception {
        expectError("Please specify a valid subcommand (get, search)");
        doCommand("site foo");
    }

    public void testErrorForNoArg() throws Exception {
        expectError("Please specify an argument for get");
        doCommand("site get");
    }

    public void testErrorForNoProviders() throws Exception {
        expectFoundProviders();
        expectError("No site providers active");
        doCommand("site get arb");
    }

    private void doCommand(String cmd) {
        mockRegistry.replayMocks();
        command.execute(cmd, out, err);
        mockRegistry.verifyMocks();
    }

    private void expectOutput(String... lines) {
        for (String line : lines) {
            out.println(line);
        }
    }

    private void expectError(String... lines) {
        for (String line : lines) {
            err.println(line);
        }
    }

    private void expectFoundProviders(SiteProvider... providers) throws InvalidSyntaxException {
        ServiceReference[] sr = new ServiceReference[providers.length];
        for (int i = 0; i < providers.length; i++) {
            sr[i] = new MockServiceReference(new MockBundle("bundle" + i));
            expect(bundleContext.getService(sr[i])).andReturn(providers[i]);
        }

        expect(bundleContext.getAllServiceReferences(SiteProvider.class.getName(), null)).
            andReturn(sr.length == 0 ? null : sr);
    }
}
