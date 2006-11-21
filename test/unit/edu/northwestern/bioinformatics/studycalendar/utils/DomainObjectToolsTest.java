package edu.northwestern.bioinformatics.studycalendar.utils;

import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.DomainObject;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Participant;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledArm;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledEvent;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudyParticipantAssignment;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.TestObject;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

import java.util.Arrays;
import java.util.List;
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

    public void testOtherTypeSpecificity() throws Exception {
        assertIsMoreSpecific(Site.class, StudySite.class);
        assertIsMoreSpecific(StudySite.class, Study.class);
    }

    public void testPlannedTypeSpecificity() {
        assertIsMoreSpecific(Study.class, PlannedCalendar.class);
        assertIsMoreSpecific(PlannedCalendar.class, Epoch.class);
        assertIsMoreSpecific(Epoch.class, Arm.class);
        assertIsMoreSpecific(Arm.class, Period.class);
        assertIsMoreSpecific(Period.class, PlannedEvent.class);
    }

    public void testScheduledTypeSpecificity() throws Exception {
        assertIsMoreSpecific(Study.class, Participant.class);
        assertIsMoreSpecific(Participant.class, StudyParticipantAssignment.class);
        assertIsMoreSpecific(StudyParticipantAssignment.class, ScheduledCalendar.class);
        assertIsMoreSpecific(ScheduledCalendar.class, ScheduledArm.class);
        assertIsMoreSpecific(ScheduledArm.class, ScheduledEvent.class);
    }

    public void testPlannedLessSpecificThanScheduled() throws Exception {
        assertIsMoreSpecific(PlannedEvent.class, StudyParticipantAssignment.class);
    }

    private void assertIsMoreSpecific(Class<? extends DomainObject> lessSpecific, Class<? extends DomainObject> moreSpecific) {
        assertTrue(moreSpecific.getName() + " should be more specific than " + lessSpecific.getName(), DomainObjectTools.isMoreSpecific(moreSpecific, lessSpecific));
        assertFalse(lessSpecific.getName() + " should be less specific than " + moreSpecific.getName(), DomainObjectTools.isMoreSpecific(lessSpecific, moreSpecific));
    }
}
