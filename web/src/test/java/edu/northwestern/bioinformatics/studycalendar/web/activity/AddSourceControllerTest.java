package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.dao.ActivityTypeDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import edu.northwestern.bioinformatics.studycalendar.domain.ActivityType;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.core.Fixtures.createNamedInstance;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.BUSINESS_ADMINISTRATOR;
import static org.easymock.EasyMock.expect;

public class AddSourceControllerTest extends ControllerTestCase {

    private SourceDao sourceDao;
    private ActivityTypeDao activityTypeDao;
    private AddSourceController controller;
    private List<ActivityType> activityTypes = new ArrayList<ActivityType>();

    private Source source1, source2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new AddSourceController();
        sourceDao = registerDaoMockFor(SourceDao.class);
        activityTypeDao = registerDaoMockFor(ActivityTypeDao.class);
        controller.setSourceDao(sourceDao);
        controller.setActivityTypeDao(activityTypeDao);

        source1 = setId(111, createNamedInstance("Test Source 1", Source.class));
        source2 = setId(222, createNamedInstance("Test Source 2", Source.class));
    }

    public void testAuthorizedRoles() {
        Collection<ResourceAuthorization> actualAuthorizations = controller.authorizations(null, null);
        assertRolesAllowed(actualAuthorizations, BUSINESS_ADMINISTRATOR);
    }

    @SuppressWarnings({ "unchecked" })
    public void testEmptyModel() throws Exception {
        request.setParameter("source", source1.getName());
        Map<String, Object> actualModel;

        expect(sourceDao.getByName(source1.getName())).andReturn(source1).anyTimes();

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Model object is not empty", actualModel.isEmpty());

    }

    @SuppressWarnings({ "unchecked" })
    public void testNotEmptyModel() throws Exception {
        request.setParameter("source", source2.getName());
        Map<String, Object> actualModel;

        expect(sourceDao.getByName(source2.getName())).andReturn(null).anyTimes();
        expect(activityTypeDao.getAll()).andReturn(activityTypes).anyTimes();
        sourceDao.save(source2);
        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("source"));
        assertTrue("Model contains the wrong object", actualModel.containsValue(source2));
    }

    @SuppressWarnings({ "unchecked" })
    public void testEmptySource() throws Exception {
        request.setParameter("source", "");
        Map<String, Object> actualModel;

        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertFalse("Model object is empty", actualModel.isEmpty());
        assertTrue("Model object does not contains error", actualModel.containsKey("error"));

    }
}
