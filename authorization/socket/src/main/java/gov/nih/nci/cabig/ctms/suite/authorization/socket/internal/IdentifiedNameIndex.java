package gov.nih.nci.cabig.ctms.suite.authorization.socket.internal;

import java.util.HashMap;
import java.util.Map;

/**
 * An thread-safe in-memory index of [String, Long] pairs. Needed in order to track the IDs for
 * PGs and PEs in {@link SuiteAuthorizationSocket}.
 * <p>
 * Implementation note: This implementation does not attempt to reclaim memory &mdash;
 * it will eventually contain strong references to name and IDs for all the scope objects in the
 * system. I do not think this will be sufficient memory to worry about in any deployed instance.
 *
 * @author Rhett Sutphin
 */
public class IdentifiedNameIndex {
    private final Map<String, IdentifiedName> nameIndex;
    private final Map<Long, IdentifiedName> idIndex;

    public IdentifiedNameIndex() {
        nameIndex = createMap();
        idIndex = createMap();
    }

    // exposed for overriding in tests
    protected <K, V> Map<K, V> createMap() {
        return new HashMap<K, V>();
    }

    public IdentifiedName get(String name) {
        if (!nameIndex.containsKey(name)) {
            insert(name);
        }
        return nameIndex.get(name);
    }

    private synchronized void insert(String name) {
        // locked double checking
        if (nameIndex.containsKey(name)) return;
        IdentifiedName newName = new IdentifiedName((long) nameIndex.size(), name);
        nameIndex.put(name, newName);
        idIndex.put(newName.getId(), newName);
    }

    public IdentifiedName get(long id) {
        return idIndex.get(id);
    }

    private synchronized long nextId() {
        return (long) nameIndex.size();
    }
}
