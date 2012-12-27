/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.osgi;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.Embedder;
import edu.northwestern.bioinformatics.studycalendar.tools.osgi.EmbedderConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.launch.FrameworkFactory;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.io.File;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class OsgiLayerEmbedderCreatorTest {
    private MockServletContext servletContext;
    private Embedder created;

    @Before
    public void before() throws Exception {
        String base = StudyCalendarTestCase.getModuleRelativeDirectory("web",
            "src/test/java/" +
                getClass().getPackage().getName().replace('.', File.separatorChar)) +
            "/embedder_test_base";
        servletContext = new MockServletContext(base, new FileSystemResourceLoader());
        created = new OsgiLayerEmbedderCreator(servletContext).create();
    }

    @Test
    public void createdEmbedderPointsToWebInfOsgiLayer() throws Exception {
        EmbedderConfiguration actual = created.getConfiguration();
        assertThat((String) actual.getFrameworkProperties().get("foo.z"), is("bar"));
    }

    @Test
    public void createdEmbedderHasFrameworkFactory() throws Exception {
        assertThat(created.getFrameworkFactory(), instanceOf(FrameworkFactory.class));
    }
}
