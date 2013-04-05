/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain.auditing;

import edu.northwestern.bioinformatics.studycalendar.domain.DomainTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import gov.nih.nci.cabig.ctms.audit.domain.Operation;
import java.text.SimpleDateFormat;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNegative;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;

/**
 * @author Jalpa Patel
 */
public class AuditEventTest extends DomainTestCase {
    private final String STUDY_URL = "/psc/pages/newStudy";
    private SimpleDateFormat sdf = DateFormat.getUTCFormat();

    public void testReverseChronologicalOrder() throws Exception {
        AuditEvent ae1 = new AuditEvent(new Study(), Operation.UPDATE,
                new DataAuditInfo("User", "10.10.10.10", sdf.parse("2010-08-17 10:40:58.361"), STUDY_URL));
        AuditEvent ae2 = new AuditEvent(new Study(), Operation.UPDATE,
                new DataAuditInfo("User", "10.10.10.10", sdf.parse("2010-08-17 10:41:58.361"), STUDY_URL));

        assertPositive(ae1.compareTo(ae2));
        assertNegative(ae2.compareTo(ae1));
    }
}
