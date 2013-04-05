/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import java.util.*;

/**
 * @author Nataliya Shurupova
 */
public class ActivityControllerTest extends ControllerTestCase {
    private SourceDao sourceDao;
    private ActivityController controller;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        sourceDao = registerDaoMockFor(SourceDao.class);

        controller = new ActivityController();
        controller.setSourceDao(sourceDao);
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }


    @SuppressWarnings({ "unchecked" })
    public void testHandleRequestInternal() throws Exception {
        Source s = Fixtures.createSource("All sources");
        Source s1 = Fixtures.createSource("Source 1");
        Source s2 = Fixtures.createSource("Source 2");
        Source s3 = Fixtures.createSource("Source 3");

        List<Source> sources = new ArrayList<Source>();
        sources.add(s);
        sources.add(s1);
        sources.add(s2);
        sources.add(s3);
        expect(sourceDao.getAll()).andReturn(sources);
        Map<String, Object> actualModel;

        replayMocks();
            actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing source object", actualModel.containsKey("sources"));
        List<Source> sourceList = (List<Source>) actualModel.get("sources");
        assertEquals("Sources aren't added ", 4, sourceList.size());
        assertTrue("Source doesn't exist ", sourceList.get(1).getName().equals(s1.getName()));
    }
}
