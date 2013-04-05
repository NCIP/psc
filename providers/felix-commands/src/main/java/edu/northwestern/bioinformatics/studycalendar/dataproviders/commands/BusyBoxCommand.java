/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dataproviders.commands;

import org.apache.felix.shell.Command;
import org.apache.commons.lang.StringUtils;

import java.io.PrintStream;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * @author Rhett Sutphin
 */
public abstract class BusyBoxCommand implements Command {
    private Map<String, Subcommand> subcommands;

    protected BusyBoxCommand() {
        subcommands = new LinkedHashMap<String, Subcommand>();
    }

    public abstract String getName();

    public abstract String getUsage();

    public abstract String getShortDescription();

    protected void addSubcommand(Subcommand command) {
        subcommands.put(command.getName(), command);
    }

    public void execute(String command, PrintStream out, PrintStream err) {
        String[] parts = command.split("\\s+", 3);

        String subcommand = parts.length > 1 ? parts[1] : null;
        if (subcommand == null) { reportInvalidSubcommand(err); return; }

        Subcommand cmd = subcommands.get(subcommand);
        if (cmd == null) { reportInvalidSubcommand(err); return; }

        String arg = parts.length > 2 ? parts[2] : null;
        if (arg == null) { reportMissingArgument(subcommand, err); return; }

        cmd.execute(arg, out, err);
    }

    private void reportInvalidSubcommand(PrintStream err) {
        err.println(String.format(
            "Please specify a valid subcommand (%s)",
            StringUtils.join(subcommands.keySet().iterator(), ", ")));
    }

    private void reportMissingArgument(String subcommandName, PrintStream err) {
        err.println(String.format("Please specify an argument for %s", subcommandName));
    }

    protected interface Subcommand {
        String getName();
        void execute(String arg, PrintStream out, PrintStream err);
    }
}
