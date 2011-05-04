package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import java.util.*;

/**
 * @author Jalpa Patel
 */
public class Differences {
    private static final String INDENT_INCREMENT = "  ";
    private List<String> messages = new ArrayList<String>();
    private Map<String, Differences> childDifferences = new HashMap<String, Differences>();

    public void addChildDifferences(String prefix, Differences differences) {
        childDifferences.put(prefix, differences);
    }

    public void addMessage(String message, String... params) {
        getMessages().add(String.format(message, params));
    }

    public <T> Differences registerValueDifference(String key, T left, T right) {
        if (!ComparisonTools.nullSafeEquals(left, right)) {
            addMessage(String.format("%s %s does not match %s",
                key, formattedValue(left), formattedValue(right)));
        }
        return this;
    }

    @SuppressWarnings( { "ChainOfInstanceofChecks" })
    private String formattedValue(Object o) {
        if (o instanceof String) {
            return String.format("\"%s\"", o);
        } else if (o instanceof Date) {
            return FormatTools.getLocal().formatDate((Date) o);
        } else {
            return o == null ? "<null>" : o.toString();
        }
    }

    public boolean hasDifferences(){
        return !getMessages().isEmpty() || !getChildDifferences().isEmpty();
    }

    public List<String> getMessages() {
        return messages;
    }

    public Map<String, Differences> getChildDifferences() {
        return childDifferences;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!getMessages().isEmpty()) {
            for (Iterator<String> it = getMessages().iterator(); it.hasNext();) {
                sb.append(it.next()).append(";");
            }
        }
        if (!getChildDifferences().isEmpty()) {
            for (Iterator entries = getChildDifferences().entrySet().iterator(); entries.hasNext();) {
                Map.Entry entry = (Map.Entry)entries.next();
                sb.append(entry.getKey()).append(" ");
                sb.append((entry.getValue()).toString());
            }
        }
        return sb.toString();
    }

    public String toTreeString() {
        StringBuilder sb = new StringBuilder();
        toTreeString(sb, "");
        return sb.toString().trim();
    }

    protected void toTreeString(StringBuilder sb, String indent) {
        for (String message : getMessages()) {
            sb.append(indent).append("* ").append(message).append('\n');
        }
        for (Map.Entry<String, Differences> entry : getChildDifferences().entrySet()) {
            sb.append(indent).append("- ").append(entry.getKey()).append('\n');
            entry.getValue().toTreeString(sb, indent + INDENT_INCREMENT);
        }
    }
}
