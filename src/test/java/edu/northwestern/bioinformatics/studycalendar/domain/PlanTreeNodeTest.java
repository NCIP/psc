package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static edu.northwestern.bioinformatics.studycalendar.test.Fixtures.*;
import edu.northwestern.bioinformatics.studycalendar.test.Fixtures;
import gov.nih.nci.cabig.ctms.domain.GridIdentifiable;
import gov.nih.nci.cabig.ctms.domain.DomainObject;

/**
 * Tests for common behaviors in {@link PlanTreeNode} and {@link PlanTreeInnerNode}.
 *
 * @author Rhett Sutphin
 */
public class PlanTreeNodeTest extends StudyCalendarTestCase {
    private Study study;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        study = Fixtures.createBasicTemplate();
    }
    
    public void testIsDetachedWhenNotDetached() throws Exception {
        assertFalse(study.getPlannedCalendar().isDetached());
        assertFalse(study.getPlannedCalendar().getEpochs().get(1).isDetached());
        assertFalse(study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(2).isDetached());
    }

    public void testIsDetachedWhenNoParent() throws Exception {
        Epoch followup = study.getPlannedCalendar().getEpochs().get(1);
        followup.setPlannedCalendar(null);
        assertTrue(followup.isDetached());
    }

    public void testIsDetachedWhenAncestorDetached() throws Exception {
        Epoch treatment = study.getPlannedCalendar().getEpochs().get(0);
        treatment.setPlannedCalendar(null);
        assertTrue(treatment.getStudySegments().get(1).isDetached());
    }

    public void testClearIds() throws Exception {
        study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(1).addPeriod(createPeriod(4, 2, 9));
        assignIds(study);

        Epoch target = study.getPlannedCalendar().getEpochs().get(0);
        target.clearIds();
        assertNullIds(target);
        assertNullIds(target.getStudySegments().get(0));
        assertNullIds(target.getStudySegments().get(1));
        assertNullIds(target.getStudySegments().get(2));
        assertNullIds(target.getStudySegments().get(1).getPeriods().first());

        assertNotNullIds(study.getPlannedCalendar());
        assertNotNullIds(study.getPlannedCalendar().getEpochs().get(1));
    }

    private <T extends GridIdentifiable & DomainObject> void assertNullIds(T node) {
        assertNull("id is set on " + node, node.getId());
        assertNull("grid id is set on " + node, node.getGridId());
    }

    private <T extends GridIdentifiable & DomainObject> void assertNotNullIds(T node) {
        assertNotNull("id should be set on " + node, node.getId());
        assertNotNull("grid id should be set on " + node, node.getGridId());
    }
}
