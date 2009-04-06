package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ControllerSecureUrlCreator;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.springframework.mock.web.MockPageContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StaticWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import static javax.servlet.jsp.tagext.Tag.*;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Rhett Sutphin
 */
public class SecureOperationTest extends WebTestCase {
    private SecureOperation secureOperation;
    private WebApplicationContext applicationContext;
    private Map<String, Role[]> secureUrls;
    private MockPageContext pageContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        secureUrls = new TreeMap<String, Role[]>(new ControllerSecureUrlCreator.PathComparator());
        secureUrls.put("/pages/cal/display/**", Role.values());
        secureUrls.put("/pages/cal/assignSubject/**", new Role[] { Role.SUBJECT_COORDINATOR });
        secureUrls.put("/pages/cal/managePeriodActivies/**", new Role[] { Role.STUDY_COORDINATOR });
        secureUrls.put("/pages/admin/sites/**", new Role[] { Role.SYSTEM_ADMINISTRATOR });

        applicationContext = ConcreteStaticApplicationContext.createWebApplicationContext(
            Collections.<String, Object>singletonMap("secureUrls", secureUrls), servletContext
        );
        request.setAttribute(
            DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("Walter", "Sobchak",
                new GrantedAuthority[] { Role.STUDY_COORDINATOR, Role.SITE_COORDINATOR })
        );
        pageContext = new MockPageContext(servletContext, request, response);

        secureOperation = new SecureOperation();
        secureOperation.setPageContext(pageContext);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        SecurityContextHolder.clearContext();
    }

    public void testNoContentWhenUnauthorized() throws Exception {
        SecurityContextHolder.clearContext();
        secureOperation.setElement("/pages/cal/display");
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }
    
    public void testBodyIncludedWhenAuthenticatedAndUrlExactlyMatches() throws Exception {
        secureOperation.setElement("/pages/cal/managePeriodActivies");
        assertEquals(EVAL_BODY_INCLUDE, secureOperation.doStartTag());
    }

    public void testBodyIncludedWhenAuthenticatedAndUrlStartsWithMatch() throws Exception {
        secureOperation.setElement("/pages/cal/display/something");
        assertEquals(EVAL_BODY_INCLUDE, secureOperation.doStartTag());
    }

    public void testBodySkippedWhenNotAuthenticatedAndUrlExactlyMatches() throws Exception {
        secureOperation.setElement("/pages/cal/assignSubject");
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }

    public void testEndTagAlwaysContinues() throws Exception {
        assertEquals(EVAL_PAGE, secureOperation.doEndTag());
    }
    
    public void testBodySkippedIfSecureUrlsMapNotAvailable() throws Exception {
        secureOperation.setElement("/pages/cal/assignSubject");
        request.setAttribute(DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, new StaticWebApplicationContext());
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }

    public void testBodySkippedIfNoElement() throws Exception {
        secureOperation.setElement(null);
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }
    
    // for OSGi transfer compatibility
    public void testRoleMatchIsNotBasedOnEnumIdentity() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("The", "Dude",
                new GrantedAuthority[] { new GrantedAuthorityImpl("SYSTEM_ADMINISTRATOR") }));
        secureOperation.setElement("/pages/admin/sites");
        assertEquals(EVAL_BODY_INCLUDE, secureOperation.doStartTag());
    }
}
