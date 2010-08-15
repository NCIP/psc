package edu.northwestern.bioinformatics.studycalendar.dao;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import static org.easymock.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class AuthorizationScopeUpdaterListenerTest extends StudyCalendarTestCase {
    private AuthorizationScopeUpdaterListener listener;

    private CsmHelper csmHelper;
    private EntityPersister studyPersister, sitePersister;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        csmHelper = registerMockFor(CsmHelper.class);

        studyPersister = registerMockFor(EntityPersister.class);
        expect(studyPersister.getPropertyNames()).andStubReturn(
            new String[] { "longTitle", "assignedIdentifier", "gridId" });
        sitePersister = registerMockFor(EntityPersister.class);
        expect(sitePersister.getPropertyNames()).andStubReturn(
            new String[] { "assignedIdentifier", "gridId", "name" });

        listener = new AuthorizationScopeUpdaterListener();
        listener.setSuiteCsmHelper(csmHelper);
    }

    public void testUpdatesStudyScopeWhenStudyAssignedIdentifierChanged() throws Exception {
        /* expect */ csmHelper.renameScopePair(ScopeType.STUDY, "114", "411");

        fireEvent(new PostUpdateEvent(new Study(), 14,
            new Object[] { "Foo", "411", "G" },
            new Object[] { "Bar", "114", "G" },
            studyPersister, null));
    }

    public void testDoesNothingWhenWhenStudyAssignedIdentifierNotChanged() throws Exception {
        fireEvent(new PostUpdateEvent(new Study(), 14,
            new Object[] { "Foo", "114", "G" },
            new Object[] { "Bar", "114", "G" },
            studyPersister, null));
    }

    public void testUpdatesSiteScopeWhenSiteAssignedIdentifierChanged() throws Exception {
        /* expect */ csmHelper.renameScopePair(ScopeType.SITE, "0309", "1220");

        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "1220", "G", "F" },
            new Object[] { "0309", "G", "C" },
            sitePersister, null));
    }

    public void testDoesNothingWhenSiteAssignedIdentifierNotChanged() throws Exception {
        fireEvent(new PostUpdateEvent(new Site(), 9,
            new Object[] { "0309", "G", "F" },
            new Object[] { "0309", "G", "C" },
            sitePersister, null));
    }

    private void fireEvent(PostUpdateEvent event) {
        replayMocks();
        listener.onPostUpdate(event);
        verifyMocks();
    }
}
