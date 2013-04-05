/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao.auditing;

import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDao;
import edu.northwestern.bioinformatics.studycalendar.dao.PeriodDaoTest;
import edu.northwestern.bioinformatics.studycalendar.domain.Duration;
import edu.northwestern.bioinformatics.studycalendar.domain.Period;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditEventValue;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;

import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createPeriod;

/**
 * @author Jalpa Patel
 */
public class AuditCreateEventListenerIntegratedTest extends AuditEventListenerTestCase {
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

    public void testCreateAuditEventAfterPeriodSave() throws Exception {
        Integer id;
        {
            Period period = createPeriod("TestPeriod", 3, Duration.Unit.week, 2, 3);
            periodDao.save(period);
            assertNotNull("not saved", period.getId());
            id = period.getId();
        }

        interruptSession();
        int eventId = getEventIdForObject(id, Period.class.getName(), Operation.CREATE.name());
        assertNotNull("Audit event for CREATE is not created ", eventId);

        DataAuditEventValue durationQuantityValue = getAuditEventValueFor(eventId, "duration.quantity");
        DataAuditEventValue durationUnitValue = getAuditEventValueFor(eventId, "duration.unit");
        DataAuditEventValue nameValue = getAuditEventValueFor(eventId, "name");

        // Testing duration.quantity
        assertNotNull("No data audit event value for duration.quantity", durationQuantityValue);
        assertEquals("Duration.quantity previous value doesn't match", null, durationQuantityValue.getPreviousValue());
        assertEquals("Duration.quantity new value doesn't match", "2", durationQuantityValue.getCurrentValue());

        // Testing duration.Unit
        assertNotNull("No data audit event value for duration.unit", durationUnitValue);
        assertEquals("Duration.Unit previous value doesn't match", null, durationUnitValue.getPreviousValue());
        assertEquals("Duration.Unit new value doesn't match", "week", durationUnitValue.getCurrentValue());

        // Testing name
        assertNotNull("No data audit event value for name", nameValue);
        assertEquals("Name previous value doesn't match", null, nameValue.getPreviousValue());
        assertEquals("Name new value doesn't match", "TestPeriod", nameValue.getCurrentValue());
    }
}
