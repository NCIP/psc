/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.DeepComparable;
import edu.northwestern.bioinformatics.studycalendar.domain.NaturallyKeyed;
import edu.northwestern.bioinformatics.studycalendar.tools.FormatTools;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.lang.ComparisonTools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jalpa Patel
 * @author Rhett Sutphin
 */
public class Differences {
    private static final String INDENT_INCREMENT = "  ";

    private static final IndexKeyExtractor<?>[] INDEX_KEY_EXTRACTORS = new IndexKeyExtractor[] {
        new IndexKeyExtractor<NaturallyKeyed>(NaturallyKeyed.class) {
            @Override public String extractKey(NaturallyKeyed value) { return value.getNaturalKey(); }
        },
        new IndexKeyExtractor<GridIdentifiable>(GridIdentifiable.class) {
            @Override public String extractKey(GridIdentifiable value) { return value.getGridId(); }
        }
    };

    private List<String> messages = new ArrayList<String>();
    private Map<String, Differences> childDifferences = new LinkedHashMap<String, Differences>();

    public void addChildDifferences(String prefix, Differences differences) {
        childDifferences.put(prefix, differences);
    }

    public void addMessage(String message, String... params) {
        getMessages().add(String.format(message, (Object[]) params));
    }

    public <T> Differences registerValueDifference(String key, T left, T right) {
        if (!ComparisonTools.nullSafeEquals(left, right)) {
            if (left instanceof Number && right instanceof Number) {
                addMessage("%s does not match: %s != %s", key, left.toString(), right.toString());
            } else {
                addMessage(String.format("%s %s does not match %s",
                    key, formattedValue(left), formattedValue(right)));
            }
        }
        return this;
    }

    @SuppressWarnings( { "ChainOfInstanceofChecks" })
    private String formattedValue(Object o) {
        if (o instanceof String) {
            return String.format("\"%s\"", o);
        } else if (o instanceof Date) {
            return FormatTools.getLocal().formatDate((Date) o);
        } else if (o instanceof NaturallyKeyed) {
            return ((NaturallyKeyed) o).getNaturalKey();
        } else {
            return o == null ? "[null]" : o.toString();
        }
    }

    public Differences registerValueCollectionDifference(String key, List<String> left, List<String> right) {
        for (String l : left) {
            if (!right.contains(l)) {
                addMessage("missing %s %s", key, l);
            }
        }
        for (String r : right) {
            if (!left.contains(r)) {
                addMessage("extra %s %s", key, r);
            }
        }
        return this;
    }

    public <T> Differences recurseDifferences(
                    // java generics are stupid
        String key, DeepComparable<T> left, T right
    ) {
        if (left != null && right != null) {
            Differences recursedDifferences = left.deepEquals(right);
            if (recursedDifferences.hasDifferences()) {
                getChildDifferences().put(key, recursedDifferences);
            }
        } else if (left != null) {
            addMessage("missing %s", key);
        } else if (right != null) {
            addMessage("extra %s", key);
        }
        return this;
    }

    @SuppressWarnings( { "unchecked" })
    public <T extends DeepComparable> Differences recurseDifferences(
        String key, Collection<T> left, Collection<T> right
    ) {
        Map<String, T> rightIndex = new LinkedHashMap<String, T>();
        for (T r : right) {
            rightIndex.put(indexKeyFor(r), r);
        }

        for (T l : left) {
            String lKey = indexKeyFor(l);
            this.recurseDifferences(key + ' ' + lKey, l, rightIndex.remove(lKey));
        }
        for (Map.Entry<String, T> entry : rightIndex.entrySet()) {
            recurseDifferences(key + ' ' + entry.getKey(), null, entry.getValue());
        }

        return this;
    }

    @SuppressWarnings( { "ChainOfInstanceofChecks", "unchecked" })
    private <T extends DeepComparable> String indexKeyFor(T t) {
        String key = null;
        for (IndexKeyExtractor extractor : INDEX_KEY_EXTRACTORS) {
            if (extractor.type().isAssignableFrom(t.getClass())) {
                key = extractor.extractKey(t);
            }
            if (key != null) return key;
        }

        return t.toString();
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

    public String toTreeHtml() {
        StringBuilder sb = new StringBuilder();
        toTreeHtml(sb, "");
        return sb.toString().trim();
    }

    private void toTreeHtml(StringBuilder sb, String indent) {
        sb.append(indent).append("<ul>\n");
        for (String message : getMessages()) {
            sb.append(indent).append("<li>").append(message).append("</li>\n");
        }
        for (Map.Entry<String, Differences> entry : getChildDifferences().entrySet()) {
            sb.append(indent).append("<li>").append(entry.getKey()).append('\n');
            entry.getValue().toTreeHtml(sb, indent + INDENT_INCREMENT);
            sb.append(indent).append("</li>\n");
        }
        sb.append(indent).append("</ul>\n");
    }

    //////

    private static abstract class IndexKeyExtractor<T> {
        private final Class<T> type;

        protected IndexKeyExtractor(Class<T> type) {
            this.type = type;
        }

        public Class<T> type() {
            return this.type;
        }

        public abstract String extractKey(T value);
    }
}
