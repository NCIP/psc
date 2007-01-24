package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.TestObject;
import org.easymock.classextension.EasyMock;
import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class WithBigIdDaoBasedEditorTest extends StudyCalendarTestCase {
    private static final Integer ID = 13;
    private static final String BIG_ID = "BIG-FAKE";
    private static final TestObject OBJECT = new TestObject(ID, BIG_ID);

    private TestObject.MockableDao dao;
    private WithBigIdDaoBasedEditor editor;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dao = registerDaoMockFor(TestObject.MockableDao.class);
        editor = new WithBigIdDaoBasedEditor(dao);
    }

    public void testSetAsId() throws Exception {
        EasyMock.expect(dao.getById(ID)).andReturn(OBJECT);

        replayMocks();
        editor.setAsText(ID.toString());
        verifyMocks();

        assertSame(OBJECT, editor.getValue());
    }

    public void testSetAsBigId() throws Exception {
        EasyMock.expect(dao.getByBigId(BIG_ID)).andReturn(OBJECT);

        replayMocks();
        editor.setAsText(BIG_ID);
        verifyMocks();

        assertSame(OBJECT, editor.getValue());
    }

    public void testSetAsTextWithInvalidId() throws Exception {
        Integer expectedId = 23;
        expect(dao.getById(expectedId)).andReturn(null);
        expect(dao.getByBigId(expectedId.toString())).andReturn(null);

        replayMocks();
        try {
            editor.setAsText(expectedId.toString());
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            verifyMocks();
            assertEquals("There is no " + TestObject.class.getSimpleName() + " with id or bigId " + expectedId, iae.getMessage());
        }
    }

    public void testSetAsTextWithInvalidNonNumericId() throws Exception {
        String expectedId = "Zipper";
        expect(dao.getByBigId(expectedId)).andReturn(null);

        replayMocks();
        try {
            editor.setAsText(expectedId);
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            verifyMocks();
            assertEquals("There is no " + TestObject.class.getSimpleName() + " with id or bigId " + expectedId, iae.getMessage());
        }
    }

    public void testGetAsText() throws Exception {
        editor.setValue(OBJECT);
        assertEquals("as text should be db ID", ID.toString(), editor.getAsText());
    }

}
