/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.commands;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.felix.shell.Command;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Rhett Sutphin
 */
public class SystemPropertyCommand implements Command {
    private SystemPropertyAccessor accessor;

    public SystemPropertyCommand() {
        this(new DefaultAccessor());
    }

    public SystemPropertyCommand(SystemPropertyAccessor accessor) {
        this.accessor = accessor;
    }

    public String getName() {
        return "sysprop";
    }

    public String getUsage() {
        return "sysprop [property.name]";
    }

    public String getShortDescription() {
        return "display the current value for one or all system properties.";
    }

    @SuppressWarnings({ "unchecked" })
    public void execute(String command, PrintStream out, PrintStream err) {
        String[] parts = command.split("\\s+");
        if (parts.length == 1) {
            List<String> propertyNames = new ArrayList<String>((Collection) accessor.getAll().keySet());
            Collections.sort(propertyNames);
            for (String name : propertyNames) {
                printProperty(out, name, accessor.getProperty(name));
            }
        } else {
            String name = parts[1];
            String value = accessor.getProperty(name);

            if (value != null) {
                printProperty(out, name, value);
            } else {
                err.println(String.format("'%s' is not set", name));
            }
        }
    }

    private void printProperty(PrintStream out, String name, String value) {
        out.println(String.format("%s=\"%s\"", name, StringEscapeUtils.escapeJava(value)));
    }

    // This is indirected for testing
    public interface SystemPropertyAccessor {
        String getProperty(String propertyName);
        Properties getAll();
    }

    private static class DefaultAccessor implements SystemPropertyAccessor {
        public String getProperty(String propertyName) {
            return System.getProperty(propertyName);
        }

        public Properties getAll() {
            return System.getProperties();
        }
    }
}
