package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import edu.northwestern.bioinformatics.studycalendar.test.MapBuilder;
import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RowPreservingInitializerTest extends SchemaInitializerTestCase {
    private static final String TABLE_NAME = "diners";

    public void testDefaultPkIsNamedId() throws Exception {
        assertEquals(Arrays.asList("id"), new RowPreservingInitializer("something").getPrimaryKeyNames());
    }

    public void testRowQueryRunBeforeAll() throws Exception {
        expectInitialRowQuery(Collections.<Integer>emptyList());
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME).beforeAll(connectionSource);
        verifyMocks();
    }

    public void testRowQueryUsesPkIfSet() throws Exception {
        expect(jdbc.queryForList("SELECT some_id FROM diners")).
            andReturn(Collections.emptyList());
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME, "some_id").beforeAll(connectionSource);
        verifyMocks();
    }

    public void testRowQueryUsesMultiplePksIfSet() throws Exception {
        expect(jdbc.queryForList("SELECT a_id, b_id FROM diners")).
            andReturn(Collections.emptyList());
        replayMocks();
        new RowPreservingInitializer(TABLE_NAME, Arrays.asList("a_id", "b_id")).beforeAll(connectionSource);
        verifyMocks();
    }

    public void testAfterEachWipesAllExceptInitiallyPresentRows() throws Exception {
        expectInitialRowQuery(Arrays.asList(1, 5, 7, 3));
        expect(jdbc.update(
            eq("DELETE FROM diners WHERE NOT ((id=?) OR (id=?) OR (id=?) OR (id=?))"),
            aryEq(new Object[] { 1, 5, 7, 3 })
        )).andReturn(4);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(TABLE_NAME);
        init.beforeAll(connectionSource);
        init.afterEach(connectionSource);
        verifyMocks();
    }

    public void testAfterEachDeleteUsesPkIfSet() throws Exception {
        expect(jdbc.queryForList("SELECT some_id FROM diners")).
            andReturn(Arrays.asList(
                Collections.singletonMap("some_id", 3),
                Collections.singletonMap("some_id", 7),
                Collections.singletonMap("some_id", 12),
                Collections.singletonMap("some_id", 88)
            ));
        expect(jdbc.update(
            eq("DELETE FROM diners WHERE NOT ((some_id=?) OR (some_id=?) OR (some_id=?) OR (some_id=?))"),
            aryEq(new Object[] { 3, 7, 12, 88 })
        )).andReturn(4);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(TABLE_NAME, "some_id");
        init.beforeAll(connectionSource);
        init.afterEach(connectionSource);
        verifyMocks();
    }

    public void testAfterEachDeleteUsesMultiplePksIfSet() throws Exception {
        expect(jdbc.queryForList("SELECT a_id, b_id FROM diners")).
            andReturn(Arrays.asList(
                new MapBuilder<String, Integer>().put("a_id", 3).put("b_id", 4).toMap(),
                new MapBuilder<String, Integer>().put("b_id", 7).put("a_id", 4).toMap()
            ));
        expect(jdbc.update(
            eq("DELETE FROM diners WHERE NOT ((a_id=? AND b_id=?) OR (a_id=? AND b_id=?))"),
            aryEq(new Object[] { 3, 4, 4, 7 })
        )).andReturn(2);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(
            TABLE_NAME, Arrays.asList("a_id", "b_id"));
        init.beforeAll(connectionSource);
        init.afterEach(connectionSource);
        verifyMocks();
    }

    public void testAfterEachDeletesEverythingIfThereAreNoIdsToPreserve() throws Exception {
        expectInitialRowQuery(Collections.<Integer>emptyList());
        expect(jdbc.update("DELETE FROM diners", (Object[]) null)).andReturn(1);
        replayMocks();
        RowPreservingInitializer init = new RowPreservingInitializer(TABLE_NAME);
        init.beforeAll(connectionSource);
        init.afterEach(connectionSource);
        verifyMocks();
    }

    private void expectInitialRowQuery(List<Integer> expectedIds) {
        List<Map<String, Integer>> expectedResult = new ArrayList<Map<String, Integer>>();
        for (Integer id : expectedIds) {
            expectedResult.add(
                Collections.singletonMap("id", id)
            );
        }
        expect(jdbc.queryForList("SELECT id FROM diners")).
            andReturn(expectedResult);
    }
}
