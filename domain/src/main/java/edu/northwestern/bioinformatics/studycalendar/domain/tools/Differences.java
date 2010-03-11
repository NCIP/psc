package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import java.util.*;

/**
 * @author Jalpa Patel
 */
public class Differences {
    private List<String> messages = new ArrayList<String>();
    private Map<String, Differences> childDifferences = new HashMap<String, Differences>();

    public void addChildDifferences(String prefix, Differences differences) {
        childDifferences.put(prefix, differences);
    }

    public void addMessage(String message) {
        getMessages().add(message);
    }

    public boolean hasDifferences(){
        if (!getMessages().isEmpty() || !getChildDifferences().isEmpty()) {
            return true;
        }
        return false;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }

    public Map<String, Differences> getChildDifferences() {
        return childDifferences;
    }

    public void setChildDifferences(Map<String, Differences> childDifferences) {
        this.childDifferences = childDifferences;
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
}
