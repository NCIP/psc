package edu.northwestern.bioinformatics.studycalendar.utils.editors;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class PscUserEditorTest extends WebTestCase {
    private PscUserEditor editor;
    private PscUserService pscUserService;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        pscUserService = registerMockFor(PscUserService.class);

        editor = new PscUserEditor(pscUserService);
    }

    public void testSetAsTextLooksUpByUsername() throws Exception {
        PscUser expectedUser = AuthorizationObjectFactory.createPscUser("fred");
        expect(pscUserService.getAuthorizableUser("fred")).andReturn(expectedUser);

        replayMocks();
        editor.setAsText("fred");
        verifyMocks();

        assertSame("Wrong user", expectedUser, editor.getValue());
    }

    public void testSetAsTextLooksThrowsIllegalArgumentExceptionForUnknownUser() throws Exception {
        expect(pscUserService.getAuthorizableUser("fred")).andReturn(null);

        try {
            replayMocks();
            editor.setAsText("fred");
            fail("Exception not thrown");
        } catch (IllegalArgumentException iae) {
            assertEquals("Wrong exception", "No user named \"fred\"", iae.getMessage());
        }
    }

    public void testGetAsTextReturnsUsername() throws Exception {
        PscUser expectedUser = AuthorizationObjectFactory.createPscUser("fred");
        editor.setValue(expectedUser);

        assertEquals("Wrong text", "fred", editor.getAsText());
    }

    public void testGetAsTextForNoneReturnsNull() throws Exception {
        editor.setValue(null);

        assertNull("Wrong text", editor.getAsText());
    }
}
