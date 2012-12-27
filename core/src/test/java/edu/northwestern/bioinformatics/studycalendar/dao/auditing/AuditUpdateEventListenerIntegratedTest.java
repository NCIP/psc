/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

/**
 * @author Jalpa Patel
 */
public class AuditUpdateEventListenerIntegratedTest extends AuditEventListenerTestCase {
    private PeriodDao periodDao;

    @Override
    protected String getTestDataFileName() {
        return String.format("../testdata/%s.xml",
            PeriodDaoTest.class.getSimpleName());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        periodDao = (PeriodDao) getApplicationContext().getBean("periodDao");
    }

    public void testAuditEventAfterPeriodUpdate() throws Exception {
        {
            Period loaded = periodDao.getById(-100);
            loaded.getDuration().setUnit(Duration.Unit.day);
            loaded.getDuration().setQuantity(10);

            periodDao.save(loaded);
        }

        interruptSession();
        int eventId = getEventIdForObject(-100, Period.class.getName(), Operation.UPDATE.name());
        assertNotNull("Audit event for UPDATE is not created ", eventId);

        DataAuditEventValue durationQuantityValue = getAuditEventValueFor(eventId, "duration.quantity");
        DataAuditEventValue durationUnitValue = getAuditEventValueFor(eventId, "duration.unit");

        // Testing duration.quantity
        assertNotNull("No data audit event value for duration.quantity", durationQuantityValue);
        assertEquals("Duration.quantity previous value doesn't match", "6", durationQuantityValue.getPreviousValue());
        assertEquals("Duration.quantity new value doesn't match", "10", durationQuantityValue.getCurrentValue());

        // Testing duration.Unit
        assertNotNull("No data audit event value for duration.unit", durationUnitValue);
        assertEquals("Duration.Unit previous value doesn't match", "week", durationUnitValue.getPreviousValue());
        assertEquals("Duration.Unit new value doesn't match", "day", durationUnitValue.getCurrentValue());
    }
}
