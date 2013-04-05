/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import edu.northwestern.bioinformatics.studycalendar.domain.tools.DateFormat;
import gov.nih.nci.security.authorization.domainobjects.User;
import java.text.SimpleDateFormat;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertNegative;
import static gov.nih.nci.cabig.ctms.testing.MoreJUnitAssertions.assertPositive;

/**
 * @author Jalpa Patel
 */
public class UserActionTest  extends DomainTestCase {
    private User csmUser;
    private SimpleDateFormat sdf = DateFormat.getUTCFormat();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        csmUser = new User();
        csmUser.setUserId(11L);
    }

    public void testEqualsTrueWhenEqualProperties() {
        UserAction userAction1 = new UserAction("description", "context", "actionType", true, csmUser);
        UserAction userAction2 = new UserAction("description", "context", "actionType", true, csmUser);
        assertTrue(userAction1.equals(userAction2));
    }

    public void testEqualsFalseWhenOneOfPropertiedIsNotEqual() throws Exception {
        UserAction userAction1 = new UserAction("description", "context", "actionType1", true, csmUser);
        UserAction userAction2 = new UserAction("description", "context", "actionType2", true, csmUser);
        assertFalse(userAction1.equals(userAction2));
    }

    public void testHashCodesEqualWhenEqual() {
        UserAction userAction1 = new UserAction("description", "context", "actionType", true, csmUser);
        UserAction userAction2 = new UserAction("description", "context", "actionType", true, csmUser);
        assertEquals(userAction1.hashCode(), userAction2.hashCode());
    }

    public void testChronologicalOrder() throws Exception {
        UserAction ua1 = new UserAction("description", "context", "actionType", true, csmUser);
        ua1.setTime(sdf.parse("2010-08-17 10:40:58.361"));
        UserAction ua2 = new UserAction("description", "context", "actionType", true, csmUser);
        ua2.setTime(sdf.parse("2010-08-17 10:41:58.361"));

        assertPositive(ua2.compareTo(ua1));
        assertNegative(ua1.compareTo(ua2));
    }
}
