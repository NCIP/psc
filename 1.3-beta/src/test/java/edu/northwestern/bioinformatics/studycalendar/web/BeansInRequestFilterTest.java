package edu.northwestern.bioinformatics.studycalendar.web;

import static org.easymock.classextension.EasyMock.expect;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;

/**
 * @author Rhett Sutphin
 */
public class BeansInRequestFilterTest extends WebTestCase {
    private WebApplicationContext applicationContext;
    private BeansInRequestFilter filter;
    private MockFilterConfig filterConfig;
    private FilterChain filterChain;

    protected void setUp() throws Exception {
        super.setUp();
        applicationContext = registerMockFor(WebApplicationContext.class);
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        filterConfig = new MockFilterConfig(servletContext);
        filterChain = registerMockFor(FilterChain.class);
        filter = new BeansInRequestFilter();
    }

    private void initFilter() throws ServletException {
        filter.init(filterConfig);
    }

    public void testFilter() throws Exception {
        initFilter();
        filterChain.doFilter(request, response);
        filter.setBeanNames(new String[] { "A", "B", "C" });
        Object[] beans = new Object[] { "1", "2", "3" };
        expect(applicationContext.getBean("A")).andReturn(beans[0]);
        expect(applicationContext.getBean("B")).andReturn(beans[1]);
        expect(applicationContext.getBean("C")).andReturn(beans[2]);

        replayMocks();
        filter.doFilter(request, response, filterChain);
        verifyMocks();

        assertEquals(request.getAttribute("A"), beans[0]);
        assertEquals(request.getAttribute("B"), beans[1]);
        assertEquals(request.getAttribute("C"), beans[2]);
    }
    
    public void testInitSplit() throws Exception {
        filterConfig.addInitParameter("beanNames", " beanOne, beanTwo, bean3 ");
        initFilter();

        assertEquals("Wrong number of bean names", 3, filter.getBeanNames().length);
        assertEquals("beanOne", filter.getBeanNames()[0]);
        assertEquals("beanTwo", filter.getBeanNames()[1]);
        assertEquals("bean3", filter.getBeanNames()[2]);
    }

    public void testInitSingle() throws Exception {
        filterConfig.addInitParameter("beanNames", "\tbean");
        initFilter();

        assertEquals("bean", filter.getBeanNames()[0]);
    }
}
