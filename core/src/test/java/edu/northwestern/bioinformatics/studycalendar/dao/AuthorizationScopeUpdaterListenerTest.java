package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationScopeUpdaterListenerTest extends StudyCalendarTestCase {
    private AuthorizationScopeUpdaterListener listener;

    private CsmHelper csmHelper;
    private PscUserService pscUserService;
    private EntityPersister studyPersister, sitePersister;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        csmHelper = registerMockFor(CsmHelper.class);
        pscUserService = registerMockFor(PscUserService.class);

        studyPersister = registerMockFor(EntityPersister.class);
        expect(studyPersister.getPropertyNames()).andStubReturn(
            new String[] { "longTitle", "assignedIdentifier", "gridId" });
        sitePersister = registerMockFor(EntityPersister.class);
        expect(sitePersister.getPropertyNames()).andStubReturn(
            new String[] { "assignedIdentifier", "gridId", "name" });

        expect(pscUserService.isAuthorizationSystemReadOnly()).andStubReturn(false);

        listener = new AuthorizationScopeUpdaterListener();
        listener.setSuiteCsmHelper(csmHelper);
        listener.setPscUserService(pscUserService);
    }

    public void testUpdatesStudyScopeWhenStudyAssignedIdentifierChanged() throws Exception {
        /* expect */ csmHelper.renameScopePair(ScopeType.STUDY, "114", "411");

        fireEvent(new PostUpdateEvent(new Study(), 14,
            new Object[] { "Foo", "411", "G" },
            new Object[] { "Bar", "114", "G" },
            new int[0], studyPersister, null));
    }

    public void testDoesNothingToStudyWhenAuthorizationSystemReadOnly() throws Exception {
        expect(pscUserService.isAuthorizationSystemReadOnly()).andReturn(true);

        fireEvent(new PostUpdateEvent(new Study(), 14,
            new Object[] { "Foo", "411", "G" },
            new Object[] { "Bar", "114", "G" },
            new int[0], studyPersister, null));
    }

    public void testDoesNothingWhenWhenStudyAssignedIdentifierNotChanged() throws Exception {
        fireEvent(new PostUpdateEvent(new Study(), 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Bar", "114", "G" },
            new int[0], studyPersister, null));
    }

    public void testUpdatesSiteScopeWhenSiteAssignedIdentifierChanged() throws Exception {
        /* expect */ csmHelper.renameScopePair(ScopeType.SITE, "0309", "1220");

        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "1220", "G", "F" },
            new Object[] { "0309", "G", "C" },
            new int[0], sitePersister, null));
    }

    public void testDoesNothingToSiteWhenAuthorizationSystemReadOnly() throws Exception {
        expect(pscUserService.isAuthorizationSystemReadOnly()).andReturn(true);

        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "1220", "G", "F" },
            new Object[] { "0309", "G", "C" },
            new int[0], sitePersister, null));
    }

    public void testDoesNothingWhenSiteAssignedIdentifierNotChanged() throws Exception {
        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "0309", "G", "F" },
            new Object[] { "0309", "G", "C" },
            new int[0], sitePersister, null));
    }

    // this shouldn't happen, but does
    public void testDoesNothingWhenOldStateUnknown() throws Exception {
        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "0309", "G", "F" },
            null,
            new int[0], sitePersister, null));
    }

    private void fireEvent(PostUpdateEvent event) {
        replayMocks();
        listener.onPostUpdate(event);
        verifyMocks();
    }
}
