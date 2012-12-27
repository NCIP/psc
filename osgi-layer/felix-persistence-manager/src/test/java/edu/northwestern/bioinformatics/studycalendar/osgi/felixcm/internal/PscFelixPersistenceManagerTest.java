/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.osgi.felixcm.internal;

import edu.northwestern.bioinformatics.studycalendar.database.StudyCalendarDbTestCase;
import org.apache.commons.collections15.EnumerationUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;

/**
 * @author Rhett Sutphin
 */
@SuppressWarnings({ "unchecked", "RawUseOfParameterizedType" })
public class PscFelixPersistenceManagerTest extends StudyCalendarDbTestCase {
    private static final String GOOD_PID = "edu.nwu.psc.words";
    private static final String BAD_PID = "edu.nwu.psc.wordle-wardle";

    private PscFelixPersistenceManager manager;
    private static final String BUNDLE_LOCATION_PROPERTY = "service.bundleLocation";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        manager = new PscFelixPersistenceManager();
        manager.initializeSessionFactory(getDataSource(), getDataSourceProperties());
    }

    @Override
    protected void tearDown() throws Exception {
        manager.destroySessionFactory();
        super.tearDown();
    }

    public void testExistsWhenDoesExist() throws Exception {
        assertTrue(manager.exists(GOOD_PID));
    }

    public void testExistsWhenDoesNotExist() throws Exception {
        assertFalse(manager.exists(BAD_PID));
    }

    public void testLoadForExisting() throws Exception {
        Dictionary actual = manager.load(GOOD_PID);
        assertNotNull(actual);
        assertTrue("Missing letters array: " + actual.get("letters"),
            actual.get("letters") instanceof char[]);
        assertEquals("Missing favorite value", "godspeed", actual.get("favorite"));
        assertEquals("Wrong number of items", 2, actual.size());
    }

    public void testLoadForNotExisting() throws Exception {
        try {
            manager.load(BAD_PID);
            fail("Exception not thrown");
        } catch (IOException actual) {
            assertEquals("No configuration for PID=\"edu.nwu.psc.wordle-wardle\"", actual.getMessage());
        }
    }

    public void testDictionaryEnumerationContainsAllDictionaries() throws Exception {
        Enumeration actual = manager.getDictionaries();
        assertTrue("Missing first", actual.hasMoreElements());
        assertDictionaryKeys((Dictionary) actual.nextElement(), "count");
        assertTrue("Missing second", actual.hasMoreElements());
        assertDictionaryKeys((Dictionary) actual.nextElement(), "letters", "favorite");
        assertFalse("Has too many", actual.hasMoreElements());
    }

    private void assertDictionaryKeys(Dictionary<String, ?> actual, String... expectedKeys) {
        Collection<String> actualKeys = EnumerationUtils.toList(actual.keys());
        for (String expectedKey : expectedKeys) {
            assertTrue("Missing key " + expectedKey, actualKeys.contains(expectedKey));
        }
        assertEquals("Wrong number of keys", expectedKeys.length, actualKeys.size());
    }

    public void testDeleteExisting() throws Exception {
        {
            manager.delete(GOOD_PID);
        }

        assertFalse(manager.exists(GOOD_PID));
    }

    public void testDeleteNotExisting() throws Exception {
        manager.delete(BAD_PID);
        // expect no exception
    }

    public void testStoreAsNew() throws Exception {
        String newPid = "edu.nwu.psc.numbers";
        {
            Dictionary d = new Hashtable();
            d.put("aleph", (byte) 0);
            d.put("nature", new int[] { 2, 72 });
            d.put("pie", new Vector(Arrays.asList("chess", "key lime")));

            manager.store(newPid, d);
        }

        Dictionary loaded = manager.load(newPid);
        assertEquals("Wrong reloaded contents: " + loaded, 3, loaded.size());
        assertEquals("Missing aleph", (byte) 0, loaded.get("aleph"));

        assertTrue("Missing nature", loaded.get("nature") instanceof int[]);
        int[] actualNature = (int[]) loaded.get("nature");
        assertEquals("Wrong value 0 in nature", 2, actualNature[0]);
        assertEquals("Wrong value 1 in nature", 72, actualNature[1]);

        assertTrue("Missing pie", loaded.get("pie") instanceof Vector);
        List actualPie = (List) loaded.get("pie");
        assertEquals("Wrong value 0 in pie", "chess", actualPie.get(0));
        assertEquals("Wrong value 1 in pie", "key lime", actualPie.get(1));
    }

    public void testStoreAsUpdate() throws Exception {
        {
            Dictionary d = manager.load(GOOD_PID);
            d.put("favorite", "mezzanine");
            d.put("first", 'W');
            d.remove("letters");
            manager.store(GOOD_PID, d);
        }

        Dictionary reloaded = manager.load(GOOD_PID);
        assertEquals("Updated value not updated", "mezzanine", reloaded.get("favorite"));
        assertNull("Removed property still present", reloaded.get("letters"));
        assertEquals("New property not present", 'W', reloaded.get("first"));
    }

    public void testLoadFiltersOutBundleLocation() throws Exception {
        String pid = "edu.nwu.psc.thoughts";
        Map<String, Object> inDb = getJdbcTemplate().queryForMap(
            "SELECT id FROM osgi_cm_properties WHERE name=? AND service_pid=?",
            new Object[] { BUNDLE_LOCATION_PROPERTY, pid });
        // The inferred type for ID is different for oracle vs. others
        assertEquals("Test setup failure", -101, ((Number) inDb.get("ID")).intValue());

        Dictionary loaded = manager.load(pid);
        assertNull("Bundle location present", loaded.get(BUNDLE_LOCATION_PROPERTY));
        assertEquals("Wrong loaded contents", 1, loaded.size());
    }
    
    public void testStoreFiltersOutBundleLocation() throws Exception {
        String newPid = "edu.nwu.psc.breakfast";
        {
            Dictionary d = new Hashtable();
            d.put("hash", "browns");
            d.put("waffle", "belgian");
            d.put(BUNDLE_LOCATION_PROPERTY, "file:/path/to/breakfast-2.0.jar");

            manager.store(newPid, d);
        }

        List actual = getJdbcTemplate().queryForList(
            "SELECT id FROM osgi_cm_properties WHERE name=? AND service_pid=?",
            new Object[] { BUNDLE_LOCATION_PROPERTY, newPid }
        );
        assertEquals("Should be no results: " + actual, 0, actual.size());
    }

    public void testModifyingALoadedDictionaryDoesNotAffectThePersistedData() throws Exception {
        Dictionary first = manager.load(GOOD_PID);
        first.put("favorite", "apocrypha");

        Dictionary second = manager.load(GOOD_PID);
        assertEquals("godspeed", second.get("favorite"));
    }
}
