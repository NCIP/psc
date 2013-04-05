/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

/**
 * @author Jalpa Patel
 */
public class AuditDeleteEventListenerIntegratedTest extends AuditEventListenerTestCase {
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

    public void testAuditEventAfterPeriodDelete() throws Exception {
        {
            Period p = periodDao.getById(-1111);
            periodDao.delete(p);
        }

        interruptSession();

        int eventId = getEventIdForObject(-1111, Period.class.getName(), Operation.DELETE.name());
        assertNotNull("Audit event for DELETE is not created ", eventId);

        DataAuditEventValue durationQuantityValue = getAuditEventValueFor(eventId, "duration.quantity");
        DataAuditEventValue durationUnitValue = getAuditEventValueFor(eventId, "duration.unit");
        DataAuditEventValue nameValue = getAuditEventValueFor(eventId, "name");
        DataAuditEventValue repetitionsValue = getAuditEventValueFor(eventId, "repetitions");
        DataAuditEventValue startDayValue = getAuditEventValueFor(eventId, "startDay");

        // Testing duration.quantity
        assertEquals("Duration.quantity previous value doesn't match", "6", durationQuantityValue.getPreviousValue());
        assertEquals("Duration.quantity new value doesn't match", null, durationQuantityValue.getCurrentValue());

        // Testing duration.Unit
        assertEquals("Duration.Unit previous value doesn't match", "week", durationUnitValue.getPreviousValue());
        assertEquals("Duration.Unit new value doesn't match", null, durationUnitValue.getCurrentValue());

        // Testing name
        assertEquals("Name previous value doesn't match", "Treatment", nameValue.getPreviousValue());
        assertEquals("Name new value doesn't match", null, nameValue.getCurrentValue());

        // Testing repetitions
        assertEquals("Repetitions previous value doesn't match", "3", repetitionsValue.getPreviousValue());
        assertEquals("Repetitions new value doesn't match", null, repetitionsValue.getCurrentValue());

        // Testing startDay
        assertEquals("StartDay previous value doesn't match", "8", startDayValue.getPreviousValue());
        assertEquals("StartDay new value doesn't match", null, startDayValue.getCurrentValue());
    }
}

