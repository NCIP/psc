/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
import gov.nih.nci.cabig.ctms.testing.MockRegistry;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.osgi.mock.MockBundleContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import static edu.northwestern.bioinformatics.studycalendar.web.osgi.OsgiLayerStartupListener.*;
import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerStartupListenerTest {
    private OsgiLayerStartupListener listener;

    private MockRegistry mocks;
    private MockServletContext servletContext;
    private Embedder mockEmbedder;

    @Before
    public void before() throws Exception {
        mocks = new MockRegistry();
        mockEmbedder = mocks.registerMockFor(Embedder.class);
        expect(mockEmbedder.start()).andStubReturn(new MockBundleContext());

        servletContext = new MockServletContext();

        listener = new OsgiLayerStartupListener() {
            @Override
            protected Embedder createEmbedder(ServletContext servletContext) {
                return mockEmbedder;
            }
        };
    }

    @Test
    public void initializeStoresEmbedderInContext() throws Exception {
        doInitialize();

        assertThat(
            (Embedder) servletContext.getAttribute(EMBEDDER_ATTRIBUTE),
            is(sameInstance(mockEmbedder)));
    }

    @Test
    public void initializeStartsEmbedder() throws Exception {
        expect(mockEmbedder.start()).andReturn(new MockBundleContext());

        doInitialize(); // verification sufficient
    }

    @Test
    public void initializeStoresReturnedBundleContextInServletContext() throws Exception {
        MockBundleContext startedContext = new MockBundleContext();
        expect(mockEmbedder.start()).andReturn(startedContext);

        doInitialize();

        assertThat(
            (BundleContext) servletContext.getAttribute(BUNDLE_CONTEXT_ATTRIBUTE),
            is(Matchers.<BundleContext>sameInstance(startedContext))
        );
    }

    private void doInitialize() {
        mocks.replayMocks();
        listener.contextInitialized(new ServletContextEvent(servletContext));
        mocks.verifyMocks();
    }

    @Test
    public void destroyStopsEmbedder() throws Exception {
        servletContext.setAttribute(EMBEDDER_ATTRIBUTE, mockEmbedder);

        /* expect */ mockEmbedder.stop();

        doDestroy();
        // verification sufficient
    }

    @Test
    public void destroyDoesNothingWithNoEmbedder() throws Exception {
        servletContext.setAttribute(EMBEDDER_ATTRIBUTE, null);

        doDestroy();
        // expect no errors
    }

    private void doDestroy() {
        mocks.replayMocks();
        listener.contextDestroyed(new ServletContextEvent(servletContext));
        mocks.verifyMocks();
    }
}
