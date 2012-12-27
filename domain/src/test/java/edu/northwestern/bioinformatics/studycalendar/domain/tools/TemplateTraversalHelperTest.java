/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.tools;

import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivity;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySegment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Add;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Changeable;
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

    public void testGetRootNodesFromDeltas() throws Exception {
        StudySegment seg;
        PlannedActivity pa;

        Study study = new Study();
        Amendment a = new Amendment();
        study.setDevelopmentAmendment(a);
        a.addDelta(
            Delta.createDeltaFor(
                new Epoch(),
                Add.create(seg = new StudySegment())
            )
        );

        a.addDelta(
            Delta.createDeltaFor(
                new Period(),
                Add.create(pa = new PlannedActivity())
            )
        );

        Collection<Changeable> actual = TemplateTraversalHelper.findAddedNodes(study);
        System.out.println(actual);
        assertEquals("Wrong size", 3, actual.size());
        assertTrue("Missing planned calendar", actual.contains(study.getPlannedCalendar()));
        assertTrue("Missing planned activity", actual.contains(pa));
        assertTrue("Missing segment", actual.contains(seg));
    }
}
