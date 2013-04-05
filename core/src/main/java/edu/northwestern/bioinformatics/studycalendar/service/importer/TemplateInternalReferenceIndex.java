/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.importer;

import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.Population;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.ChildrenChange;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import gov.nih.nci.cabig.ctms.domain.MutableDomainObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jalpa Patel
 */
public class TemplateInternalReferenceIndex {
    private Map<Key, Entry> index;

    public TemplateInternalReferenceIndex() {
        this.index = new HashMap<Key, Entry>();
    }

    public Entry get(Key key) {
        if (!index.containsKey(key)) {
            index.put(key, new Entry(key));
        }
        return index.get(key);
    }

    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public void addChangeable(Changeable node) {
        addObject(node);
        if (node instanceof Parent) {
            for (Object child : ((Parent) node).getChildren()) {
                if (child instanceof Changeable) {
                    addChangeable((Changeable) child);
                }
            }
        }
    }

    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public void addDelta(Delta delta) {
        addObject(delta);
        if (delta.getNode().getGridId() != null) {
            Key nodeKey = new Key(delta.getNode().getClass(), delta.getNode().getGridId());
            get(nodeKey).addDeltaReference(delta);
        }
    }

    public void addChildrenChange(ChildrenChange childrenChange) {
        addObject(childrenChange);
        if (childrenChange.getChild() != null && childrenChange.getChild().getGridId() != null) {
            // Population from xml study will not have grid id.
            // Required for identical map for existing study and study from xml.
            if (!(childrenChange.getChild() instanceof Population)) {
                Key childKey = new Key(childrenChange.getChild().getClass(), childrenChange.getChild().getGridId());
                get(childKey).addChildrenChangeReference(childrenChange);
            }
        }
    }

    private void addObject(MutableDomainObject object) {
        if (object.getGridId() != null) {
            Key key = new Key(object.getClass(), object.getGridId());
            get(key).setOriginal(object);
        }
    }

    public Map<Key, Entry> getIndex() {
        return index;
    }

    @SuppressWarnings( { "RawUseOfParameterizedType" })
    public static class Entry {
        private Key key;
        private Object original;
        private List<Delta> referringDeltas =  new ArrayList<Delta>();
        private List<ChildrenChange> referringChanges =  new ArrayList<ChildrenChange>();

        public Entry(Key key) {
            this.key = key;
        }

        public Key getKey() {
            return key;
        }

        public Object getOriginal() {
            return original;
        }

        public void setOriginal(Object original) {
            this.original = original;
        }

        public void addDeltaReference(Delta delta) {
            getReferringDeltas().add(delta);
        }

        public List<Delta> getReferringDeltas() {
            return referringDeltas;
        }

        public void addChildrenChangeReference(ChildrenChange childrenChange) {
            getReferringChanges().add(childrenChange);
        }

        public List<ChildrenChange> getReferringChanges() {
            return referringChanges;
        }
    }

    public static class Key {
        private Class<? extends MutableDomainObject> kind;
        private String id;

        public Key(Class<? extends MutableDomainObject> kind, String id) {
            this.kind = kind;
            this.id = id;
        }

        public Class<? extends MutableDomainObject> getKind() {
            return kind;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (id != null ? !id.equals(key.id) : key.id != null) return false;
            if (kind != null ? !kind.equals(key.kind) : key.kind != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = kind != null ? kind.hashCode() : 0;
            result = 31 * result + (id != null ? id.hashCode() : 0);
            return result;
        }
    }
}

