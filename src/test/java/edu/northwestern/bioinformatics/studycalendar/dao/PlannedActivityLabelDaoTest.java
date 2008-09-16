package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

import java.util.List;

/**
 * @author Nataliya Shurupova
 */
public class PlannedActivityLabelDaoTest extends ContextDaoTestCase<PlannedActivityLabelDao> {

    public void testGetById() throws Exception {
        PlannedActivityLabel loaded = getDao().getById(-29);
        assertEquals("Wrong id", -29, (int) loaded.getId());
        assertEquals("Wrong Label Id number", -15, (int) loaded.getLabel().getId());
        assertEquals("Wrong Label Name", "Label1", loaded.getLabel().getName());

        assertEquals("Wrong Planned Activity Id", -12, (int) loaded.getPlannedActivity().getId());
        assertEquals("Wrong Repetition Number", 2, (int) loaded.getRepetitionNumber());
    }


    public void testGetAllPlannedActivityLabels() throws Exception {
        List<PlannedActivityLabel> plannedActivityLabels = getDao().getAll();
        assertNotNull(plannedActivityLabels);
        assertTrue(plannedActivityLabels.size() > 0);
        assertEquals("Wrong list size", 4, plannedActivityLabels.size());
    }

    public void testGetPlannedActivityLabelsBYLabelId() throws Exception {
        List<PlannedActivityLabel> plannedActivityLabels = getDao().getByLabelId(-15);
        assertNotNull(plannedActivityLabels);
        assertTrue(plannedActivityLabels.size() > 0);
        assertEquals("Wrong list size", 2, plannedActivityLabels.size());
    }


    public void testGetPlannedActivityLabelsByPlanndeActivityId() throws Exception {
        List<PlannedActivityLabel> plannedActivityLabels = getDao().getByPlannedActivityId(-12);
        assertNotNull(plannedActivityLabels);
        assertTrue(plannedActivityLabels.size() > 0);
        assertEquals("Wrong list size", 3, plannedActivityLabels.size());
    }

    

    public void testGetRepetitionsByPlannedActivityIdAndLabelId() throws Exception {
        List<Object> repetitions = getDao().getRepetitionsByPlannedActivityIdAndLabelId(-12, -17);
        assertNotNull(repetitions);
        assertTrue(repetitions.size() > 0);
        assertEquals("Wrong list size", 2, repetitions.size());
        assertEquals("Wrong repetition number in the list", 2, repetitions.get(0));
        assertEquals("Wrong repetition number in the list", 4, repetitions.get(1));
    }


    public void testGetPALabelByPlannedActivityIdAndLabelId() throws Exception {
        List<PlannedActivityLabel> plannedActivityLabels = getDao().getPALabelByPlannedActivityIdAndLabelId(-12, -17);
        assertNotNull(plannedActivityLabels);
        assertTrue(plannedActivityLabels.size() > 0);
        assertEquals("Wrong list size", 2, plannedActivityLabels.size());
        assertEquals("Wrong expected paLabelId", -30, (int) plannedActivityLabels.get(0).getId());
        assertEquals("Wrong expected paLabelId", -31, (int) plannedActivityLabels.get(1).getId());
    }


    public void testGetPALabelByPAIdLabelIdAndRepNumber() throws Exception {
        PlannedActivityLabel plannedActivityLabel = getDao().getPALabelByPlannedActivityIdLabelIdRepNum(-12, -17, 4);
        assertNotNull(plannedActivityLabel);
        assertEquals("Wrong expected id", -31, (int) plannedActivityLabel.getId());
    }
}
