package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.AbstractDomainObject;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.List;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DomainObjectToolsTest extends StudyCalendarTestCase {
    public void testById() throws Exception {
        List<TestObject> objs = Arrays.asList(
            new TestObject(5),
            new TestObject(17),
            new TestObject(-4)
        );
        Map<Integer, TestObject> actual = DomainObjectTools.byId(objs);

        assertEquals(objs.size(), actual.size());
        for (Map.Entry<Integer, TestObject> entry : actual.entrySet()) {
            assertEquals("Wrong entry for key " + entry.getKey(),
                entry.getKey(), entry.getValue().getId());
        }
    }
    
    public void testExternalObjectId() {
        assertEquals("edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectToolsTest$TestObject.14",
            DomainObjectTools.createExternalObjectId(new TestObject(14)));
    }

    public void testExternalObjectIdRequiresId() throws Exception {
        try {
            DomainObjectTools.createExternalObjectId(new TestObject());
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals(
                "Cannot create an external object ID for a transient instance of edu.northwestern.bioinformatics.studycalendar.utils.DomainObjectToolsTest$TestObject", iae.getMessage());
        }
    }

    private static class TestObject extends AbstractDomainObject {

        public TestObject() { }

        public TestObject(int id) {
            setId(id);
        }
    }
}
