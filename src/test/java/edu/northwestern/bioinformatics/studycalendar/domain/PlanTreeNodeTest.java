package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;

/**
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
}
