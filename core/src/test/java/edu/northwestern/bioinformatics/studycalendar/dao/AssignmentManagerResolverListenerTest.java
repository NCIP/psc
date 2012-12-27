/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.CsmUserCache;
import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySubjectAssignment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.hibernate.event.PostLoadEvent;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class AssignmentManagerResolverListenerTest extends StudyCalendarTestCase {
    private AssignmentManagerResolverListener listener;

    private PostLoadEvent event;
    private StudySubjectAssignment assignment;
    private CsmUserCache csmUserCache;
    private User csmUser;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        assignment = new StudySubjectAssignment();
        csmUser = AuthorizationObjectFactory.createCsmUser(55L, "jo");

        event = new PostLoadEvent(null);
        event.setEntity(assignment);

        csmUserCache = registerMockFor(CsmUserCache.class);

        listener = new AssignmentManagerResolverListener();
        listener.setCsmUserCache(csmUserCache);
    }

    public void testManagerResolvedWhenResolveable() throws Exception {
        assignment.setManagerCsmUserId(55);
        expect(csmUserCache.getCsmUser(55)).andReturn(csmUser);

        fire();
        assertSame(csmUser, assignment.getStudySubjectCalendarManager());
    }

    public void testNotResolvedWhenNoManagerCsmId() throws Exception {
        assignment.setManagerCsmUserId(null);
        fire();
        assertNull(assignment.getStudySubjectCalendarManager());
    }

    public void testNotErrorWhenCsmReturnsNull() throws Exception {
        assignment.setManagerCsmUserId(78);
        expect(csmUserCache.getCsmUser(78)).andReturn(null);
        fire();
        assertEquals("ID reset", (Object) 78, assignment.getManagerCsmUserId());
    }

    public void testNothingHappensForNonAssignmentObjects() throws Exception {
        event.setEntity(new Study());
        fire();
    }

    private void fire() {
        replayMocks();
        listener.onPostLoad(event);
        verifyMocks();
    }
}
