/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.service.delta;

import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Delta;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateService;

import java.util.*;

import static org.easymock.EasyMock.expect;

/**
 * @author Nataliya Shurupova
 */
public class DeltaIteratorTest extends DomainTestCase {
    private TemplateService templateService;

   protected void setUp() throws Exception {
        super.setUp();
        templateService = registerMockFor(TemplateService.class);
    }

    public void testDetailOrder(){
        Study s = Fixtures.createReleasedTemplate();
        Period period = Fixtures.createPeriod("period1", 1, 10, 2);
        PlannedActivity plannedActivity = Fixtures.createPlannedActivity("activity", 3);
        PlannedActivityLabel plannedActivityLabel = Fixtures.createPlannedActivityLabel("label");
        period.addPlannedActivity(plannedActivity);
        plannedActivity.addPlannedActivityLabel(plannedActivityLabel);
        s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().add(period);

        Delta studyDelta = Delta.createDeltaFor(s.getPlannedCalendar().getStudy());
        Delta epochDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0));
        Delta studySegmentDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1));
        Delta periodDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).
                getPeriods().last());
        Delta plannedActivityDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).
                getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0));
        Delta plannedActivityLabelDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).
                getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getPlannedActivityLabels().first());

        List<Delta<?>> listOfDeltas = new ArrayList<Delta<?>>();
        listOfDeltas.add(plannedActivityDelta);
        listOfDeltas.add(periodDelta);
        listOfDeltas.add(epochDelta);
        listOfDeltas.add(studyDelta);
        listOfDeltas.add(studySegmentDelta);
        listOfDeltas.add(plannedActivityLabelDelta);

        expect(templateService.findEquivalentChild(s, studyDelta.getNode())).andReturn(s);
        expect(templateService.findEquivalentChild(s, periodDelta.getNode())).andReturn(null);
        expect(templateService.findEquivalentChild(s, epochDelta.getNode())).andReturn(epochDelta.getNode());
        expect(templateService.findEquivalentChild(s, studySegmentDelta.getNode())).andReturn(studySegmentDelta.getNode());
        expect(templateService.findEquivalentChild(s, plannedActivityDelta.getNode())).andReturn(null);
        expect(templateService.findEquivalentChild(s, plannedActivityLabelDelta.getNode())).andReturn(plannedActivityLabelDelta.getNode());
        replayMocks();
        DeltaIterator di = new DeltaIterator(listOfDeltas, s, templateService, false);

        assertSame("studyDelta is not the first element", studyDelta, di.next());
        assertSame("epochDelta is not the second element", epochDelta, di.next());
        assertSame("studySegmentDelta is not the third element", studySegmentDelta, di.next());
        assertSame("periodDelta is not the forth element", plannedActivityLabelDelta, di.next());
        assertFalse("deltaIterator has more elements ", di.hasNext());
        verifyMocks();
    }

    public void testDetailOrderReversed(){
        Study s = Fixtures.createReleasedTemplate();
        Period period = Fixtures.createPeriod("period1", 1, 10, 2);
        PlannedActivity plannedActivity = Fixtures.createPlannedActivity("activity", 3);
        PlannedActivityLabel plannedActivityLabel = Fixtures.createPlannedActivityLabel("label");
        period.addPlannedActivity(plannedActivity);
        plannedActivity.addPlannedActivityLabel(plannedActivityLabel);
        s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).getPeriods().add(period);

        Delta studyDelta = Delta.createDeltaFor(s.getPlannedCalendar().getStudy());
        Delta epochDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0));
        Delta studySegmentDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1));
        Delta periodDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0).
                getPeriods().last());
        Delta plannedActivityDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).
                getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0));
        Delta plannedActivityLabelDelta = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).
                getStudySegments().get(0).getPeriods().first().getPlannedActivities().get(0).getPlannedActivityLabels().first());

        List<Delta<?>> listOfDeltas = new ArrayList<Delta<?>>();
        listOfDeltas.add(plannedActivityDelta);
        listOfDeltas.add(periodDelta);
        listOfDeltas.add(epochDelta);
        listOfDeltas.add(studyDelta);
        listOfDeltas.add(studySegmentDelta);
        listOfDeltas.add(plannedActivityLabelDelta);

        expect(templateService.findEquivalentChild(s, studyDelta.getNode())).andReturn(s);
        expect(templateService.findEquivalentChild(s, periodDelta.getNode())).andReturn(null);
        expect(templateService.findEquivalentChild(s, epochDelta.getNode())).andReturn(epochDelta.getNode());
        expect(templateService.findEquivalentChild(s, studySegmentDelta.getNode())).andReturn(studySegmentDelta.getNode());
        expect(templateService.findEquivalentChild(s, plannedActivityDelta.getNode())).andReturn(null);
        expect(templateService.findEquivalentChild(s, plannedActivityLabelDelta.getNode())).andReturn(plannedActivityLabelDelta.getNode());

        replayMocks();

        DeltaIterator di = new DeltaIterator(listOfDeltas, s, templateService, true);

        assertSame("studyDelta is not the first element", plannedActivityLabelDelta, di.next());
        assertSame("epochDelta is not the second element", studySegmentDelta, di.next());
        assertSame("studySegmentDelta is not the third element", epochDelta, di.next());
        assertSame("periodDelta is not the forth element", studyDelta, di.next());
        assertFalse("deltaIterator has more elements ", di.hasNext());
        verifyMocks();
    }



    public void testDetailOrderWithEmptyList(){
        assertFalse(new DeltaIterator(Collections.<Delta<?>>emptyList(), null, null, false).hasNext());
    }

    public void testRemoveDetachedNodesWhileIterating() {
        Study s = Fixtures.createReleasedTemplate();
        Delta pc = Delta.createDeltaFor(s.getPlannedCalendar());
        Delta epoch2 = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0));
        StudySegment segment1 = s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1);
        Delta ss1 = Delta.createDeltaFor(segment1);
        Delta ss2 = Delta.createDeltaFor(s.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(2));
        List<Delta<?>> deltaList = new ArrayList<Delta<?>>();
        deltaList.add(ss1);
        deltaList.add(epoch2);
        deltaList.add(pc);
        deltaList.add(ss2);

        expect(templateService.findEquivalentChild(s, pc.getNode())).andReturn(pc.getNode());
        expect(templateService.findEquivalentChild(s, epoch2.getNode())).andReturn(epoch2.getNode());
        expect(templateService.findEquivalentChild(s, ss1.getNode())).andReturn(ss1.getNode());
        expect(templateService.findEquivalentChild(s, ss2.getNode())).andReturn(ss2.getNode());

        replayMocks();
        DeltaIterator di = new DeltaIterator(deltaList, s, templateService, false);
        List<Delta> encountered = new LinkedList<Delta>();
        while (di.hasNext()) {
            Delta delta = di.next();
            encountered.add(delta);

            if (delta == epoch2) {
                segment1.setParent(null);
            }
        }

        assertEquals("Wrong number of deltas encountered: " + encountered, 3, encountered.size());
        assertEquals("Wrong delta 0", pc, encountered.get(0));
        assertEquals("Wrong delta 1", epoch2, encountered.get(1));
        assertEquals("Wrong delta 2", ss2, encountered.get(2));
        verifyMocks();
    }
}
