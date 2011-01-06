package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.security.authorization.domainobjects.User;

/**
 * @author Jalpa Patel
 */
public class UserActionTest  extends DomainTestCase {
    private User csmUser;
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
}
