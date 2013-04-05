/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.commands;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Rhett Sutphin
 */
public class SystemPropertyCommandTest extends TestCase {
    private SystemPropertyCommand defaultCommand;

    private List<String> outLines;
    private List<String> errLines;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultCommand = new SystemPropertyCommand();

        outLines = new ArrayList<String>();
        errLines = new ArrayList<String>();
    }

    public void testName() throws Exception {
        assertEquals("sysprop", defaultCommand.getName());
    }

    public void testUsage() throws Exception {
        assertEquals("sysprop [property.name]", defaultCommand.getUsage());
    }

    public void testShortDescription() throws Exception {
        assertEquals("display the current value for one or all system properties.", defaultCommand.getShortDescription());
    }

    public void testGetSingleRealProperty() throws Exception {
        executeWithActualSystemProperties("sysprop path.separator");

        assertOutLines("path.separator=\"" + System.getProperty("path.separator") + '"');
        assertErrLines();
    }

    public void testGetAllRealProperties() throws Exception {
        executeWithActualSystemProperties("sysprop");
        assertErrLines();

        assertTrue("There should be some output", outLines.size() > 0);
        for (String out : outLines) {
            if (out.startsWith("os.name")) return;
        }
        fail("Missing default system property 'os.name'");
    }

    public void testErrorForUnknownProperty() throws Exception {
        executeWithTheseProperties("sysprop foo", Collections.<String, String>emptyMap());
        assertOutLines();
        assertErrLines("'foo' is not set");
    }

    public void testAllPropertiesArePrintedSorted() throws Exception {
        executeWithTheseProperties("sysprop", new MapBuilder<String, String>().
            put("c", "foo").
            put("a", "bar").
            put("t", "baz").
            toMap());
        assertErrLines();
        assertOutLines(
            "a=\"bar\"", "c=\"foo\"", "t=\"baz\""
        );
    }

    public void testPropertyValuesAreEscaped() throws Exception {
        executeWithTheseProperties("sysprop line.separator", Collections.singletonMap("line.separator", "\r"));
        assertErrLines();
        assertOutLines("line.separator=\"\\r\"");
    }

    private void assertOutLines(String... expectedOut) {
        assertLines("out", outLines, expectedOut);
    }

    private void assertErrLines(String... expectedErr) {
        assertLines("err", errLines, expectedErr);
    }

    private void assertLines(String kind, List<String> actual, String... expected) {
        assertEquals("Wrong number of " + kind + " lines: " + actual, expected.length, actual.size());
        for (int i = 0; i < expected.length; i++) {
            assertEquals(kind + " mismatch at line " + i, expected[i], actual.get(i));
        }
    }

    private void executeWithActualSystemProperties(String line) {
        execute(defaultCommand, line);
    }

    private void executeWithTheseProperties(String line, Map<String, String> properties) {
        execute(new SystemPropertyCommand(new TestingAccessor(properties)), line);
    }

    private void execute(SystemPropertyCommand command, String line) {
        ByteArrayOutputStream outOS = new ByteArrayOutputStream(1024), errOS = new ByteArrayOutputStream(1024);
        PrintStream out = new PrintStream(outOS), err = new PrintStream(errOS);
        command.execute(line, out, err);
        extractLines(outOS, outLines);
        extractLines(errOS, errLines);
    }

    private void extractLines(ByteArrayOutputStream srcStream, List<String> target) {
        String src = srcStream.toString();
        if (src.length() > 0) {
            target.addAll(Arrays.asList(src.split(System.getProperty("line.separator"))));
        }
    }

    private class TestingAccessor implements SystemPropertyCommand.SystemPropertyAccessor {
        private Properties all;

        private TestingAccessor(Map<String, String> values) {
            this.all = new Properties();
            all.putAll(values);
        }

        public String getProperty(String propertyName) {
            return all.getProperty(propertyName);
        }

        public Properties getAll() {
            return all;
        }
    }
}
