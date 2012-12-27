/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.service.ImportActivitiesService;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import static org.easymock.classextension.EasyMock.*;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * @author Rhett Sutphin
 */
public class SampleActivitySourceInitializerTest extends SchemaInitializerTestCase {
    private Resource xml;
    private ImportActivitiesService importService;
    private SampleActivitySourceInitializer initializer;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        importService = registerMockFor(ImportActivitiesService.class);
        xml = registerMockFor(Resource.class);

        initializer = new SampleActivitySourceInitializer();
        initializer.setImportActivitiesService(importService);
        initializer.setXmlResource(xml);
    }

    public void testInitializerIsRowPreservingForSources() throws Exception {
        assertTrue(initializer instanceof RowPreservingInitializer);
        assertEquals("sources", initializer.getTableName());
        assertEquals(Arrays.asList("id"), initializer.getPrimaryKeyNames());
    }

    public void testInitializerLoadsActivitiesOneTime() throws Exception {
        InputStream fakeInput = new InputStream() {
            @Override public int read() throws IOException { return 0; }
        };
        expect(xml.getInputStream()).andReturn(fakeInput);
        expect(importService.loadAndSave(fakeInput)).andReturn(null /* DC */);

        replayMocks();
        initializer.oneTimeSetup(connectionSource);
        verifyMocks();
    }
}
