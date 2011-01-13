package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Parent;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import junit.framework.TestCase;

import java.util.Collection;

/*
* @author John Dzak
*/
public class TemplateTraversalHelperTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testGetParentTreeNodesFromDeltas() throws Exception {
        StudySegment seg; PlannedCalendar pCal ;

        Amendment a = new Amendment();
        a.addDelta(
            Delta.createDeltaFor(
                new Study(),
                Add.create(pCal = new PlannedCalendar())
            )
        );

        a.addDelta(
            Delta.createDeltaFor(
                new PlannedCalendar(),
                Add.create(seg = new StudySegment())
            )
        );

        Collection<Parent> actual = TemplateTraversalHelper.findRootParentNodes(a);
        assertEquals("Wrong size", 2, actual.size());
        assertTrue("Missing planned calendar", actual.contains(pCal));
        assertTrue("Missing planned segment", actual.contains(seg));
    }
}
