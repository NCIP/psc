/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.domain.PlannedActivityLabel;

/**
 * @author Nataliya Shurupova
 */
public class PlannedActivityLabelDaoTest extends ContextDaoTestCase<PlannedActivityLabelDao> {
    public void testGetById() throws Exception {
        PlannedActivityLabel loaded = getDao().getById(-29);
        assertEquals("Wrong id", -29, (int) loaded.getId());
        assertEquals("Wrong label", "label1", loaded.getLabel());

        assertEquals("Wrong planned activity", -12, (int) loaded.getPlannedActivity().getId());
        assertEquals("Wrong repetition number", 2, (int) loaded.getRepetitionNumber());
    }
}
