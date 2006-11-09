package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.ControllerTools;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.ModelAndView;
import org.easymock.classextension.EasyMock;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbInterceptorTest extends WebTestCase {
    private BreadcrumbCreator creator;
    private BreadcrumbInterceptor interceptor;

    private Study study;

    protected void setUp() throws Exception {
        super.setUp();
        creator = registerMockFor(BreadcrumbCreator.class);

        interceptor = new BreadcrumbInterceptor();
        interceptor.setBreadcrumbCreator(creator);

        study = Fixtures.createSingleEpochStudy("S", "E", "A1", "A2");
    }

    public void testCreateContext() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        ControllerTools.addHierarchyToModel(epoch, model);
        BreadcrumbContext context = interceptor.createContext(model);
        assertSame(epoch, context.getEpoch());
        assertNull(context.getArm());
    }

    public void testIntercept() throws Exception {
        ModelAndView mv = new ModelAndView("test", "modelObject", 17);
        CrumbSource handler = new TestCrumbSource(new TestCrumb("crumb", null, null));
        List<Anchor> expectedBreadcrumbs = new ArrayList<Anchor>();
        expect(creator.createAnchors(same(handler), (BreadcrumbContext) notNull())).andReturn(expectedBreadcrumbs);

        replayMocks();
        interceptor.postHandle(request, response, handler, mv);
        verifyMocks();

        assertEquals("Wrong number of entries in model after interceptor", 2, mv.getModel().size());
        assertEquals("Original model entry missing", mv.getModel().get("modelObject"), 17);
        assertSame("breadcrumbs missing", mv.getModel().get("breadcrumbs"), expectedBreadcrumbs);
    }

    public void testInterceptNonCrumbSourceHandler() throws Exception {
        replayMocks();
        interceptor.postHandle(request, response, new Object(), new ModelAndView());
        verifyMocks();
        // no errors
    }
}
