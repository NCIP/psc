/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;

import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class TableOrdererTest extends StudyCalendarTestCase {
    private MockDbMetadata metadata;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        metadata = new MockDbMetadata();
    }

    public void testBasicReordering() throws Exception {
        metadata.link("studies", "planned_calendars");
        metadata.link("planned_calendars", "epochs", "scheduled_calendars");
        metadata.link("epochs", "study_segments");

        String[] actualOrder = doReorder(
            "epochs", "study_segments", "scheduled_calendars", "studies", "planned_calendars"
        );

        assertPartialOrder(actualOrder, "studies", "planned_calendars", "scheduled_calendars", "epochs", "study_segments");
        assertPartialOrder(actualOrder, "planned_calendars", "scheduled_calendars", "epochs", "study_segments");
        assertPartialOrder(actualOrder, "scheduled_calendars", "study_segments");
        assertPartialOrder(actualOrder, "epochs", "study_segments");
    }

    public void testReorderingRetainsLoneTables() throws Exception {
        metadata.solo("loner");
        metadata.link("alpha", "beta", "gamma");

        String[] actualOrder = doReorder(
            "gamma", "alpha", "loner", "beta"
        );

        assertPartialOrder(actualOrder, "alpha", "beta", "gamma");
        assertPresent(actualOrder, "loner");
    }
    
    public void testReorderingRetainsSelfReferencingTable() throws Exception {
        metadata.link("csm_protection_group", "csm_protection_group");
        metadata.link("studies", "planned_calendars");

        String[] actualOrder = doReorder(
            "studies", "csm_protection_group", "planned_calendars"
        );

        assertPresent(actualOrder, "csm_protection_group");
        assertPartialOrder(actualOrder, "studies", "planned_calendars");
    }

    public void testOrderingIncludesAllTablesIfNoneAreSpecified() throws Exception {
        metadata.solo("loner");
        metadata.link("alpha", "beta", "gamma");

        String[] actualOrder = doReorder();

        assertPartialOrder(actualOrder, "alpha", "beta", "gamma");
        assertPresent(actualOrder, "loner");
    }

    public void testOrderingIncludesOnlyTheTablesFromTheSchemaIncludingBeringVersionIfThereAreMultipleSchemas() throws Exception {
        metadata.solo("a", "b", "BERING_VERSION");
        metadata.schema("FOO", "a", "BERING_VERSION");
        metadata.schema("BAR", "b");

        String[] actualOrder = doReorder();

        assertPresent(actualOrder, "a");
        assertPresent(actualOrder, "BERING_VERSION");
        assertNotPresent(actualOrder, "b");
    }

    private String[] doReorder(String... tables) throws SQLException {
        return new TableOrderer(metadata, tables.length == 0 ? null : tables).insertionOrder();
    }

    private static void assertPresent(String[] actualOrder, String expected) {
        assertNonnegative("Missing " + expected + " from " + Arrays.asList(actualOrder),
            search(expected, actualOrder));
    }

    private static void assertNotPresent(String[] actualOrder, String expected) {
        assertNegative("Expected " + expected + " to be absent from " + Arrays.asList(actualOrder),
            search(expected, actualOrder));
    }

    private static void assertPartialOrder(String[] actualOrder, String expectedEarlier, String... expectedLater) {
        for (String later : expectedLater) {
            assertTableOrder(actualOrder, expectedEarlier, later);
        }
    }

    private static void assertTableOrder(String[] actualOrder, String expectedEarlier, String expectedLater) {
        int earlierIndex = search(expectedEarlier, actualOrder);
        int laterIndex = search(expectedLater, actualOrder);
        assertNonnegative("Earlier string - " + expectedEarlier + " - not present", earlierIndex);
        assertNonnegative("Later string - " + expectedLater + " - not present", laterIndex);
        assertTrue(expectedEarlier + " is after " + expectedLater, earlierIndex < laterIndex);
    }

    private static int search(String target, String[] order) {
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(target)) return i;
        }
        return -1;
    }
}
