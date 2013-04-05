/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import edu.northwestern.bioinformatics.studycalendar.dataproviders.api.StudyProvider;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
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
public class StudyCommandTest extends TestCase {
    private StudyCommand command;
    private MockRegistry mockRegistry;
    private StudyProvider sp1, sp2;
    private BundleContext bundleContext;
    private PrintStream out, err;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockRegistry = new MockRegistry();
        sp1 = mockRegistry.registerMockFor(StudyProvider.class);
        sp2 = mockRegistry.registerMockFor(StudyProvider.class);
        bundleContext = mockRegistry.registerMockFor(BundleContext.class);
        out = mockRegistry.registerMockFor(PrintStream.class);
        err = mockRegistry.registerMockFor(PrintStream.class);

        command = new StudyCommand(bundleContext);
    }

    public void testName() throws Exception {
        assertEquals("study", command.getName());
    }

    public void testUsage() throws Exception {
        assertEquals("study search <arg>", command.getUsage());
    }

    public void testShortDescription() throws Exception {
        assertEquals("execute study provider methods.", command.getShortDescription());
    }

    public void testOnlySearchIsSupported() throws Exception {
        expectError("Please specify a valid subcommand (search)");
        doCommand("study get foo");
    }

    public void testSearchWithNoProviders() throws Exception {
        expectFoundProviders();
        expectError("No study providers active");
        doCommand("study search any");
    }

    public void testSearchOutput() throws Exception {
        expectFoundProviders(sp1);

        Study study = createNamedInstance("NCT100", Study.class);
        study.setLongTitle("A very many things");
        addSecondaryIdentifier(study, "A", "100");
        addSecondaryIdentifier(study, "Current", "14V");
        addSecondaryIdentifier(study, "E", "ECOG-100");
        expect(sp1.search("100")).andReturn(Arrays.asList(study));

        expectOutput(
            "bundle0",
            "- Study assigned=NCT100",
            "  longTitle=A very many things",
            "  secondary identifiers=[",
            "    type=A       value=100",
            "    type=Current value=14V",
            "    type=E       value=ECOG-100",
            "  ]"
        );

        doCommand("study search 100");
    }

    public void testSearchExecutesAgainstAllProviders() throws Exception {
        expectFoundProviders(sp1, sp2);
        expect(sp1.search("Boo")).andReturn(Collections.<Study>emptyList());
        expect(sp2.search("Boo")).andReturn(Collections.<Study>emptyList());

        expectOutput(
            "bundle0",
            "  No matches",
            "bundle1",
            "  No matches"
        );

        doCommand("study search Boo");
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

    private void expectFoundProviders(StudyProvider... providers) throws InvalidSyntaxException {
        ServiceReference[] sr = new ServiceReference[providers.length];
        for (int i = 0; i < providers.length; i++) {
            sr[i] = new MockServiceReference(new MockBundle("bundle" + i));
            expect(bundleContext.getService(sr[i])).andReturn(providers[i]);
        }

        expect(bundleContext.getAllServiceReferences(StudyProvider.class.getName(), null)).
            andReturn(sr.length == 0 ? null : sr);
    }
}
