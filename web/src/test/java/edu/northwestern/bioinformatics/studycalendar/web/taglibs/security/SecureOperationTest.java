/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.taglibs.security;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.SecurityContextHolderTestHelper;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.AuthorizationObjectFactory;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import edu.northwestern.bioinformatics.studycalendar.tools.spring.ConcreteStaticApplicationContext;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.TestingAuthorizedHandler;
import org.acegisecurity.context.SecurityContextHolder;
import org.springframework.mock.web.MockPageContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static javax.servlet.jsp.tagext.Tag.*;

/**
 * @author Rhett Sutphin
 */
public class SecureOperationTest extends WebTestCase {
    private SecureOperation secureOperation;
    private WebApplicationContext applicationContext;
    private SimpleUrlHandlerMapping handlerMapping;
    private MockPageContext pageContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(new MapBuilder<String, Object>().
            put("/cal/display", TestingAuthorizedHandler.all()).
            put("/cal/assignSubject", new TestingAuthorizedHandler(SUBJECT_MANAGER)).
            put("/cal/managePeriodActivies", new TestingAuthorizedHandler(STUDY_CALENDAR_TEMPLATE_BUILDER)).
            put("/cal/admin/sites", new TestingAuthorizedHandler(PERSON_AND_ORGANIZATION_INFORMATION_MANAGER)).
            toMap());

        applicationContext = ConcreteStaticApplicationContext.createWebApplicationContext(
            new MapBuilder<String, Object>().
                put("urlMapping", handlerMapping).
                toMap(),
            servletContext
        );
        request.setAttribute(
            DispatcherServlet.WEB_APPLICATION_CONTEXT_ATTRIBUTE, applicationContext);

        handlerMapping.setApplicationContext(applicationContext);
        handlerMapping.initApplicationContext();

        SecurityContextHolderTestHelper.setSecurityContext(
            AuthorizationObjectFactory.createPscUser("Walter",
                STUDY_CALENDAR_TEMPLATE_BUILDER,
                STUDY_SITE_PARTICIPATION_ADMINISTRATOR),
            "Sobchak"
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

    public void testBodyIncludedWhenAuthorizedForAll() throws Exception {
        secureOperation.setElement("/pages/cal/display");
        assertEquals(EVAL_BODY_INCLUDE, secureOperation.doStartTag());
    }

    public void testBodySkippedWhenNotAuthenticatedAndUrlExactlyMatches() throws Exception {
        secureOperation.setElement("/pages/cal/assignSubject");
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }

    public void testEndTagAlwaysContinues() throws Exception {
        assertEquals(EVAL_PAGE, secureOperation.doEndTag());
    }
    
    public void testBodySkippedIfNoElement() throws Exception {
        secureOperation.setElement(null);
        assertEquals(SKIP_BODY, secureOperation.doStartTag());
    }
}
