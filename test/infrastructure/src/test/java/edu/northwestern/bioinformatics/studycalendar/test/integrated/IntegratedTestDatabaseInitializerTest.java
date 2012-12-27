/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.test.TableOrderer;
import org.easymock.classextension.EasyMock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class IntegratedTestDatabaseInitializerTest extends StudyCalendarTestCase {
    private SampleIntegratedTestDatabaseInitializer initializer;
    private TableOrderer mockOrderer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mockOrderer = registerMockFor(TableOrderer.class); 
        initializer = new SampleIntegratedTestDatabaseInitializer();
    }
    
    public void testTableInitializersAreCreatedWithTheRunId() throws Exception {
        expectTableOrder("studies");
        List<SchemaInitializer> actual = initializer.getInitializerSeries();
        assertEquals("Series is wrong length", 1, actual.size());
        String actualRunIdent = ((RecordingSchemaInitializer) actual.get(0)).getTestRunIdentifier();
        assertNotNull("Initializer has no run identifier",
            actualRunIdent);
        assertTrue("Run identifier has wrong format",
            actualRunIdent.startsWith(SampleIntegratedTestDatabaseInitializer.class.getSimpleName()));
    }

    public void testTableInitializersAreCreatedForTheTablesInTheDatabase() throws Exception {
        expectTableOrder("studies");
        List<SchemaInitializer> actual = initializer.getInitializerSeries();
        assertEquals("Series is wrong length", 1, actual.size());
        assertInitializerPresent("studies", actual);
    }
    
    public void testNullTableInitializersAreStrippedFromSeries() throws Exception {
        expectTableOrder("sites", "study_sites");
        initializer.noInitializerFor("sites");
        List<SchemaInitializer> actual = initializer.getInitializerSeries();
        assertEquals("Series is wrong length", 1, actual.size());
        assertInitializerPresent("study_sites", actual);
    }

    public void testTableInitializersAreCreatedInTableOrdererOrder() throws Exception {
        useActivityTables();
        List<SchemaInitializer> actual = initializer.getInitializerSeries();
        assertEquals("Series is wrong length", 4, actual.size());
        assertInitializerBefore("activities", "planned_activities", actual);
        assertInitializerBefore("activity_types", "activities", actual);
        assertInitializerBefore("sources", "activities", actual);
    }

    public void testOneTimeSetupInvokedInInsertionOrder() throws Exception {
        useActivityTables();
        initializer.oneTimeSetup();
        assertLifecycleMethodCalledInInsertionOrder("oneTimeSetup", 4);
    }

    public void testBeforeAllInvokedInInsertionOrder() throws Exception {
        useActivityTables();
        initializer.beforeAll();
        assertLifecycleMethodCalledInInsertionOrder("beforeAll", 4);
    }

    public void testBeforeEachInvokedInInsertionOrder() throws Exception {
        useActivityTables();
        initializer.beforeEach();
        assertLifecycleMethodCalledInInsertionOrder("beforeEach", 4);
    }

    public void testAfterEachInvokedInDeletionOrder() throws Exception {
        useActivityTables();
        initializer.afterEach();
        assertLifecycleMethodCalledInDeletionOrder("afterEach", 4);
    }

    public void testAfterAllInvokedInDeletionOrder() throws Exception {
        useActivityTables();
        initializer.afterAll();
        assertLifecycleMethodCalledInDeletionOrder("afterAll", 4);
    }

    private void assertLifecycleMethodCalledInInsertionOrder(String expectedMethod, int expectedCount) {
        List<SchemaInitializer> actualInitializers = initializer.getInitializerSeries();
        assertEquals("Wrong number of initializers", expectedCount, actualInitializers.size());
        for (int i = 0 ; i < expectedCount - 1 ; i++) {
            ((RecordingSchemaInitializer) actualInitializers.get(i + 1)).assertCalledAfter(expectedMethod,
                ((RecordingSchemaInitializer) actualInitializers.get(i)).getFirstCallTime(expectedMethod));
        }
    }

    private void assertLifecycleMethodCalledInDeletionOrder(String expectedMethod, int expectedCount) {
        List<SchemaInitializer> actualInitializers = initializer.getInitializerSeries();
        assertEquals("Wrong number of initializers", expectedCount, actualInitializers.size());
        for (int i = 0 ; i < expectedCount - 1 ; i++) {
            Long nextInitializerCallTime = ((RecordingSchemaInitializer) actualInitializers.get(i + 1)).getFirstCallTime(expectedMethod);
            assertNotNull(String.format("Initializer %d never called", i + 1), nextInitializerCallTime);
            ((RecordingSchemaInitializer) actualInitializers.get(i)).assertCalledAfter(expectedMethod,
                nextInitializerCallTime);
        }
    }

    private void useActivityTables() {
        expectTableOrder("sources", "activity_types", "activities", "planned_activities");
    }

    private void expectTableOrder(String... tables) {
        EasyMock.expect(mockOrderer.insertionOrder()).andStubReturn(tables);
        replayMocks();
    }

    private static List<String> tableOrder(List<SchemaInitializer> actualInitializers) {
        List<String> tables = new ArrayList<String>(actualInitializers.size());
        for (SchemaInitializer actualInitializer : actualInitializers) {
            tables.add(((RecordingSchemaInitializer) actualInitializer).getTableName());
        }
        return tables;
    }

    public static void assertInitializerPresent(String expectedName, List<SchemaInitializer> actualRecordingInitializers) {
        assertContains(tableOrder(actualRecordingInitializers), expectedName);
    }

    public static void assertInitializerBefore(
        String expectedEarlierTable, String expectedLaterTable, List<SchemaInitializer> actualRecordingInitializers
    ) {
        assertInitializerPresent(expectedEarlierTable, actualRecordingInitializers);
        assertInitializerPresent(expectedLaterTable, actualRecordingInitializers);

        List<String> order = tableOrder(actualRecordingInitializers);
        int actualEarlierIndex = order.indexOf(expectedEarlierTable);
        int actualLaterIndex = order.indexOf(expectedLaterTable);

        assertTrue(String.format("Expected %s to be earlier than %s in %s", expectedEarlierTable, expectedLaterTable, order),
            actualEarlierIndex < actualLaterIndex);
    }

    private class SampleIntegratedTestDatabaseInitializer extends IntegratedTestDatabaseInitializer {
        private Set<String> skipTables = new HashSet<String>();

        public void noInitializerFor(String... tableNames) {
            skipTables.addAll(Arrays.asList(tableNames));
        }
        
        @Override
        public SchemaInitializer getTableInitializer(String tableName) {
            if (skipTables.contains(tableName)) {
                return null;
            } else {
                return new RecordingSchemaInitializer(tableName, getTestRunIdentifier());
            }
        }

        @Override
        protected TableOrderer createTableOrderer() {
            return mockOrderer;
        }
    }

    private static class RecordingSchemaInitializer implements SchemaInitializer {
        private final Logger log = LoggerFactory.getLogger(getClass());

        private String testRunIdentifier;
        private String tableName;
        private Map<String, List<Long>> calls;

        public RecordingSchemaInitializer(String tableName, String testRunIdentifier) {
            calls = new HashMap<String, List<Long>>();
            this.testRunIdentifier = testRunIdentifier;
            this.tableName = tableName;
        }

        public String getTestRunIdentifier() {
            return testRunIdentifier;
        }

        public String getTableName() {
            return tableName;
        }

        public Long getFirstCallTime(String method) {
            if (calls.containsKey(method) && !calls.get(method).isEmpty()) {
                return calls.get(method).get(0);
            } else {
                return null;
            }
        }

        public void oneTimeSetup(ConnectionSource connectionSource) { recordCall("oneTimeSetup"); }
        public void beforeAll(ConnectionSource connectionSource)    { recordCall("beforeAll");    }
        public void beforeEach(ConnectionSource connectionSource)   { recordCall("beforeEach");   }
        public void afterEach(ConnectionSource connectionSource)    { recordCall("afterEach");    }
        public void afterAll(ConnectionSource connectionSource)     { recordCall("afterAll");     }

        private void recordCall(String method) {
            if (!calls.containsKey(method)) { calls.put(method, new ArrayList<Long>()); }
            calls.get(method).add(System.currentTimeMillis());
            // ensure that no calls happen in the same ms for testing ordering
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.debug("Sleep interrupted");
            }
        }

        public void assertCalled(String method) {
            assertNotNull(method + " never called for " + tableName, calls.get(method));
            assertFalse(method + " never called for " + tableName, calls.get(method).isEmpty());
        }

        public void assertCalledAfter(String method, Long expectedTime) {
            assertNotNull("Test construction failure: expected time is null", expectedTime);
            assertCalled(method);
            List<Long> callTimes = calls.get(method);
            boolean after = false;
            for (Long callTime : callTimes) {
                if (expectedTime < callTime) after = true;
            }
            assertTrue(method + " called at " + callTimes + ", all of which are before " + expectedTime, after);
        }
    }
}
