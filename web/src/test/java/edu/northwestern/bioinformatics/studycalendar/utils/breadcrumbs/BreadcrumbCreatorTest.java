/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.service.TestingTemplateService;
import edu.northwestern.bioinformatics.studycalendar.service.DomainContext;
import gov.nih.nci.cabig.ctms.tools.spring.ControllerUrlResolver;
import gov.nih.nci.cabig.ctms.tools.spring.ResolvedControllerReference;

import java.util.Map;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import java.util.LinkedHashMap;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.easymock.classextension.EasyMock;

/**
 * @author Rhett Sutphin
 */
public class BreadcrumbCreatorTest extends StudyCalendarTestCase {
    private static final TestCrumb C1 = new TestCrumb("C1", null, null);
    private static final TestCrumb C2 = new TestCrumb("C2", null, C1);
    private static final TestCrumb C3 = new TestCrumb("C3", Collections.singletonMap("studySegment", "1"), C1);
    private static final TestCrumb C4 = new TestCrumb("C4", null, C2);
    private static TestCrumb C5;
    static {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("cal", "54");
        params.put("zed", "45");
        C5 = new TestCrumb("C5", params, C3);
    }
    private static final List<TestCrumb> CRUMBS = Arrays.asList(C1, C3, C2, C4, C5);

    private BreadcrumbCreator breadcrumbCreator;

    private ControllerUrlResolver urlResolver;
    private DefaultListableBeanFactory beanFactory;
    private DomainContext context;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        urlResolver = registerMockFor(ControllerUrlResolver.class);
        context = new DomainContext(new TestingTemplateService());

        breadcrumbCreator = new BreadcrumbCreator();
        breadcrumbCreator.setUrlResolver(urlResolver);

        beanFactory = new DefaultListableBeanFactory();
        for (TestCrumb crumb : CRUMBS) {
            beanFactory.registerSingleton(crumb.getName(null), new TestCrumbSource(crumb));
        }
        breadcrumbCreator.postProcessBeanFactory(beanFactory);
    }

    public void testAnchorsAtRoot() throws Exception {
        expectResolve(C1);

        List<Anchor> anchors = doGetAnchors(C1);

        assertEquals(1, anchors.size());
        assertAnchorForCrumb(C1, anchors.get(0));
    }

    public void testMultilevelAnchors() throws Exception {
        expectResolve(C1, C2, C4);

        List<Anchor> actual = doGetAnchors(C4);
        assertEquals(3, actual.size());
        assertAnchorForCrumb(C1, actual.get(0));
        assertAnchorForCrumb(C2, actual.get(1));
        assertAnchorForCrumb(C4, actual.get(2));
    }
    
    public void testAnchorWithParams() throws Exception {
        expectResolve(C1, C3, C5);

        List<Anchor> actual = doGetAnchors(C5);
        assertEquals(3, actual.size());
        assertAnchorForCrumb(C1, actual.get(0));
        assertAnchorForCrumb(C3, "?studySegment=1", actual.get(1));
        assertAnchorForCrumb(C5, "?cal=54&zed=45", actual.get(2));
    }

    private List<Anchor> doGetAnchors(Crumb start) {
        replayMocks();
        List<Anchor> anchors = breadcrumbCreator.createAnchors(new TestCrumbSource(start), context);
        verifyMocks();
        return anchors;
    }

    private void expectResolve(TestCrumb... expectedCrumbs) {
        for (TestCrumb expectedCrumb : expectedCrumbs) {
            String name = expectedCrumb.getName(null);
            EasyMock.expect(urlResolver.resolve(name))
                .andReturn(new ResolvedControllerReference(name, TestCrumb.class.getName(), "", "/url/" + name.toLowerCase()));
        }
    }

    private void assertAnchorForCrumb(TestCrumb expected, Anchor actual) {
        assertAnchorForCrumb(expected, "", actual);
    }

    private void assertAnchorForCrumb(TestCrumb expected, String expectedQueryString, Anchor actual) {
        String expectedName = expected.getName(context);
        assertEquals("Wrong anchor text", expectedName, actual.getText());
        assertEquals("Wrong url", "//url/" + expectedName.toLowerCase() + expectedQueryString, actual.getUrl());
    }

}
