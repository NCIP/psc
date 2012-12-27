/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.core.Fixtures;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;

import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;
import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbInterceptorTest extends WebTestCase {
    private BreadcrumbCreator creator;
    private BreadcrumbInterceptor interceptor;

    private Study study;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        creator = registerMockFor(BreadcrumbCreator.class);

        interceptor = new BreadcrumbInterceptor();
        interceptor.setBreadcrumbCreator(creator);
        interceptor.setTemplateService(new TestingTemplateService());

        study = Fixtures.createSingleEpochStudy("S", "E", "A1", "A2");
    }

    public void testCreateContext() throws Exception {
        Map<String, Object> model = new HashMap<String, Object>();
        Epoch epoch = study.getPlannedCalendar().getEpochs().get(0);
        controllerTools.addHierarchyToModel(epoch, model);
        DomainContext context = interceptor.createContext(model);
        assertSame(epoch, context.getEpoch());
        assertNull(context.getStudySegment());
    }

    public void testIntercept() throws Exception {
        ModelAndView mv = new ModelAndView("test", "modelObject", 17);
        CrumbSource handler = testHandler();
        List<Anchor> expectedBreadcrumbs = new ArrayList<Anchor>();
        expect(creator.createAnchors(same(handler), (DomainContext) notNull())).andReturn(expectedBreadcrumbs);

        replayMocks();
        interceptor.postHandle(request, response, handler, mv);
        verifyMocks();

        assertEquals("Wrong number of entries in model after interceptor", 3, mv.getModel().size());
        assertEquals("Original model entry missing", mv.getModel().get("modelObject"), 17);
        assertSame("breadcrumbs missing", mv.getModel().get("breadcrumbs"), expectedBreadcrumbs);
    }

    public void testInterceptNonCrumbSourceHandler() throws Exception {
        replayMocks();
        ModelAndView mv = new ModelAndView();
        interceptor.postHandle(request, response, new Object(), mv);
        verifyMocks();
        assertEquals(0, mv.getModel().size());
    }
    
    public void testInterceptRedirectByName() throws Exception {
        ModelAndView mv = new ModelAndView("redirectToCalendarTemplate");
        replayMocks();
        interceptor.postHandle(request, response, testHandler(), mv);
        verifyMocks();
        assertEquals(0, mv.getModel().size());
    }
    
    public void testInterceptRedirectView() throws Exception {
        ModelAndView mv = new ModelAndView(new RedirectView("target"));
        replayMocks();
        interceptor.postHandle(request, response, testHandler(), mv);
        verifyMocks();
        assertEquals(0, mv.getModel().size());
    }

    public void testInterceptWithNoModelAndView() throws Exception {
        replayMocks();
        interceptor.postHandle(request, response, testHandler(), null);
        verifyMocks();
        // no exceptions
    }

    private TestCrumbSource testHandler() {
        return new TestCrumbSource(new TestCrumb("crumb", null, null));
    }
}
