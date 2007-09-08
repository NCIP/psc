package edu.northwestern.bioinformatics.studycalendar.domain.delta;

import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.*;

/**
 * @author Rhett Sutphin
 */
public class AddTest extends StudyCalendarTestCase {
    private Add add;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        add = new Add();
    }

    public void testGetNewChildIdPassesThroughFromDomainObject() throws Exception {
        Epoch child = setId(4, new Epoch());
        add.setNewChildId(17);
        add.setNewChild(child);
        assertEquals(4, (int) add.getNewChildId());
    }
    
    public void testGetNewChildUsesDirectAttributeIfNoDomainObject() throws Exception {
        add.setNewChild(null);
        add.setNewChildId(5);
        assertEquals(5, (int) add.getNewChildId());
    }

    public void testSetNewChildIdClearsNewChildIfIdsDoNotMatch() throws Exception {
        add.setNewChild(setId(3, new Arm()));
        add.setNewChildId(15);
        assertNull("New child not cleared", add.getNewChild());
    }

    public void testSetNewChildIdKeepsNewChildIfIdsMatch() throws Exception {
        Arm expectedChild = setId(15, new Arm());
        add.setNewChild(expectedChild);
        add.setNewChildId(expectedChild.getId());
        assertSame("New child incorrectly cleared", expectedChild, add.getNewChild());
    }
}
