package edu.northwestern.bioinformatics.studycalendar.domain;

import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.*;
import gov.nih.nci.cabig.ctms.dao.DomainObjectDao;
import gov.nih.nci.cabig.ctms.domain.DomainObject;
import org.easymock.classextension.EasyMock;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class DomainObjectToolsTest extends DomainTestCase {
    private Comparator<TestObject> byIdComparator = new DomainObjectTools.ById<TestObject>();

    private TestObject o1, o2;

    protected void setUp() throws Exception {
        super.setUp();
        o1 = new TestObject(1);
        o2 = new TestObject(2);
    }

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
    
    public void testByIdComparator() throws Exception {
        assertPositive(byIdComparator.compare(o2, o1));
        assertNegative(byIdComparator.compare(o1, o2));
    }

    public void testByIdComparatorWhenEqual() throws Exception {
        o2.setId(1);
        assertEquals(0, byIdComparator.compare(o1, o2));
        assertEquals(0, byIdComparator.compare(o2, o1));
    }

    public void testByIdComparatorNullSafe() throws Exception {
        o1.setId(null);
        assertNegative(byIdComparator.compare(o1, o2));
        assertPositive(byIdComparator.compare(o2, o1));
        assertEquals(0, byIdComparator.compare(o1, o1));
    }

    public void testExternalObjectId() {
        assertEquals("edu.northwestern.bioinformatics.studycalendar.domain.TestObject.14",
            DomainObjectTools.createExternalObjectId(new TestObject(14)));
    }

    public void testExternalObjectIdRequiresId() throws Exception {
        try {
            DomainObjectTools.createExternalObjectId(new TestObject());
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals(
                "Cannot create an external object ID for a transient instance of edu.northwestern.bioinformatics.studycalendar.domain.TestObject", iae.getMessage());
        }
    }

    public void testParseExternalObjectId() throws Exception {
        assertEquals(34, DomainObjectTools
            .parseExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.TestObject.34"));
    }

    public void testParseCreatedExternalObjectId() throws Exception {
        String created = DomainObjectTools.createExternalObjectId(new TestObject(14));
        assertEquals(14, DomainObjectTools.parseExternalObjectId(created));
    }

    public void testLoadFromExternalObjectId() throws Exception {
        TestObject expected = new TestObject(66);
        DomainObjectDao<TestObject> dao = registerMockFor(TestObject.MockableDao.class);
        EasyMock.expect(dao.getById(66)).andReturn(expected);

        replayMocks();
        assertSame(expected, DomainObjectTools
            .loadFromExternalObjectId("edu.northwestern.bioinformatics.studycalendar.domain.TestObject.66", dao));
        verifyMocks();
    }

    public void testOtherTypeSpecificity() throws Exception {
        assertIsMoreSpecific(Site.class, StudySite.class);
        assertIsMoreSpecific(StudySite.class, Study.class);
    }

    public void testPlannedTypeSpecificity() {
        assertIsMoreSpecific(Study.class, PlannedCalendar.class);
        assertIsMoreSpecific(PlannedCalendar.class, Epoch.class);
        assertIsMoreSpecific(Epoch.class, StudySegment.class);
        assertIsMoreSpecific(StudySegment.class, Period.class);
        assertIsMoreSpecific(Period.class, PlannedActivity.class);
    }

    public void testSpecificityWithDynamicSubclasses() {
        PlannedCalendar dynamic = new PlannedCalendar() { };
        assertNotEquals("Test setup failure", PlannedCalendar.class, dynamic.getClass());
        assertIsMoreSpecific(Study.class, dynamic.getClass());
        assertIsMoreSpecific(dynamic.getClass(), Epoch.class);
    }

    public void testScheduledTypeSpecificity() throws Exception {
        assertIsMoreSpecific(Study.class, Subject.class);
        assertIsMoreSpecific(Subject.class, StudySubjectAssignment.class);
        assertIsMoreSpecific(StudySubjectAssignment.class, ScheduledCalendar.class);
        assertIsMoreSpecific(ScheduledCalendar.class, ScheduledStudySegment.class);
        assertIsMoreSpecific(ScheduledStudySegment.class, ScheduledActivity.class);
    }

    public void testPlannedLessSpecificThanScheduled() throws Exception {
        assertIsMoreSpecific(PlannedActivity.class, StudySubjectAssignment.class);
    }

    public void testMoreSpecificThanSelf() throws Exception {
        assertFalse(DomainObjectTools.isMoreSpecific(Study.class, Study.class));
    }

    public void testUnknownIsLessSpecificThanEverything() throws Exception {
        DomainObject anon = new DomainObject() {
            public Integer getId() { throw new UnsupportedOperationException("getId not implemented"); }
            public void setId(Integer integer) { throw new UnsupportedOperationException("setId not implemented"); }
        };

        assertIsMoreSpecific(anon.getClass(), Site.class);
        assertIsMoreSpecific(anon.getClass(), Study.class);
        assertIsMoreSpecific(anon.getClass(), ScheduledActivity.class);
    }

    private void assertIsMoreSpecific(Class<? extends DomainObject> lessSpecific, Class<? extends DomainObject> moreSpecific) {
        assertTrue(moreSpecific.getName() + " should be more specific than " + lessSpecific.getName(), DomainObjectTools.isMoreSpecific(moreSpecific, lessSpecific));
        assertFalse(lessSpecific.getName() + " should be less specific than " + moreSpecific.getName(), DomainObjectTools.isMoreSpecific(lessSpecific, moreSpecific));
    }
}
