/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.TreeMap;

/**
 * Base class for controlled vocabulary items, implemented in java as a typesafe enumeration.
 * The IDs, at least, must be manually kept in sync with the lookup table in the database.
 *
 * @author Rhett Sutphin
 */
public class AbstractControlledVocabularyObject implements Comparable<AbstractControlledVocabularyObject> {
    private static Map<Class<? extends AbstractControlledVocabularyObject>, Map<Integer, ? extends AbstractControlledVocabularyObject>> BY_ID
        = new HashMap<Class<? extends AbstractControlledVocabularyObject>, Map<Integer, ? extends AbstractControlledVocabularyObject>>();

    private final int id;
    private final String name;

    protected AbstractControlledVocabularyObject(int id, String name) {
        this.id = id;
        this.name = name;
        register(this);
    }

    private static <T extends AbstractControlledVocabularyObject> void register(T instance) {
        Class<T> clazz = (Class<T>) instance.getClass();
        getByIdMap(clazz).put(instance.getId(), instance);
    }

    private static <T extends AbstractControlledVocabularyObject> Map<Integer, T> getByIdMap(Class<T> clazz) {
        if (!BY_ID.containsKey(clazz)) {
            BY_ID.put(clazz, new TreeMap<Integer, T>());
        }
        return (Map<Integer, T>) BY_ID.get(clazz);
    }

    protected static <T extends AbstractControlledVocabularyObject> T getById(Class<T> clazz, int id) {
        return getByIdMap(clazz).get(id);
    }

    protected static <T extends AbstractControlledVocabularyObject> Collection<T> values(Class<T> clazz) {
        return getByIdMap(clazz).values();
    }

    public int compareTo(AbstractControlledVocabularyObject o) {
        return getId() - o.getId();
    }

    public final int getId() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public String toString() {
        return getName();
    }
}
