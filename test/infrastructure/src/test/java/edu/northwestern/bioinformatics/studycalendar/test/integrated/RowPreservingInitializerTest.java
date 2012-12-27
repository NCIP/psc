/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import java.util.Arrays;

import static edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer.PK_RECORD_TABLE_NAME;
import static org.easymock.classextension.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class RowPreservingInitializerTest extends SchemaInitializerTestCase {
    private static final String TABLE_NAME = "diners";

    public void testDefaultPkIsNamedId() throws Exception {
        assertEquals(Arrays.asList("id"), new RowPreservingInitializer("something").getPrimaryKeyNames());
    }

    public void testOneTimeSetupCreatesRowRecordTempTableIfItDoesNotExist() throws Exception {
        expect(jdbc.execute(new RowPreservationTableCreator(3, jdbc))).andReturn(null);
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME, Arrays.asList("a_id", "b_id", "c_id")).
            oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testRowQueryRunBeforeAll() throws Exception {
        expect(jdbc.update("DELETE FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners'")).
            andReturn(0);
        expect(jdbc.update("INSERT INTO " + PK_RECORD_TABLE_NAME +
            " (table_name, key0) SELECT 'diners', id FROM diners")).andReturn(2);
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME).beforeAll(connectionSource);
        verifyMocks();
    }

    public void testRowQueryUsesPkIfSet() throws Exception {
        expect(jdbc.update("DELETE FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners'")).
            andReturn(0);
        expect(jdbc.update("INSERT INTO " + PK_RECORD_TABLE_NAME +
            " (table_name, key0) SELECT 'diners', some_id FROM diners")).andReturn(2);
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME, "some_id").beforeAll(connectionSource);
        verifyMocks();
    }

    public void testRowQueryUsesMultiplePksIfSet() throws Exception {
        expect(jdbc.update("DELETE FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners'")).
            andReturn(0);
        expect(jdbc.update("INSERT INTO " + PK_RECORD_TABLE_NAME +
            " (table_name, key0, key1) SELECT 'diners', a_id, b_id FROM diners")).
            andReturn(3);
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME, Arrays.asList("a_id", "b_id")).beforeAll(connectionSource);
        verifyMocks();
    }

    public void testAfterEachWipesAllExceptInitiallyPresentRows() throws Exception {
        expect(jdbc.update(
            "DELETE FROM diners WHERE (CAST (id AS VARCHAR(32))) NOT IN " +
                "(SELECT key0 FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners')"
        )).andReturn(4);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(TABLE_NAME);
        init.afterEach(connectionSource);
        verifyMocks();
    }

    public void testAfterEachDeleteUsesPkIfSet() throws Exception {
        expect(jdbc.update(
            "DELETE FROM diners WHERE (CAST (some_id AS VARCHAR(32))) NOT IN " +
                "(SELECT key0 FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners')"
        )).andReturn(4);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(TABLE_NAME, "some_id");
        init.afterEach(connectionSource);
        verifyMocks();
    }

    public void testAfterEachDeleteUsesMultiplePksIfSet() throws Exception {
        expect(jdbc.update(
            "DELETE FROM diners WHERE (CAST (a_id AS VARCHAR(32)), CAST (b_id AS VARCHAR(32))) NOT IN " +
                "(SELECT key0, key1 FROM " + PK_RECORD_TABLE_NAME + " WHERE table_name='diners')"
        )).andReturn(4);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(
            TABLE_NAME, Arrays.asList("a_id", "b_id"));
        init.afterEach(connectionSource);
        verifyMocks();
    }
}
