package edu.northwestern.bioinformatics.studycalendar.web.activity;

import edu.northwestern.bioinformatics.studycalendar.web.ControllerTestCase;
import edu.northwestern.bioinformatics.studycalendar.dao.SourceDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Source;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.setId;
import static edu.northwestern.bioinformatics.studycalendar.domain.Fixtures.createNamedInstance;
import java.util.Map;
import static org.easymock.EasyMock.expect;

public class AddSourceControllerTest extends ControllerTestCase {

    private SourceDao sourceDao;
    private AddSourceController controller;

    private Source source1, source2;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        controller = new AddSourceController();
        sourceDao = registerDaoMockFor(SourceDao.class);
        controller.setSourceDao(sourceDao);

        source1 = setId(111, createNamedInstance("Test Source 1", Source.class));
        source2 = setId(222, createNamedInstance("Test Source 2", Source.class));
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
        sourceDao.save(source2);
        replayMocks();
        actualModel = controller.handleRequestInternal(request, response).getModel();
        verifyMocks();

        assertTrue("Missing model object", actualModel.containsKey("source"));
        assertTrue("Model contains the wrong object", actualModel.containsValue(source2));
    }
}
