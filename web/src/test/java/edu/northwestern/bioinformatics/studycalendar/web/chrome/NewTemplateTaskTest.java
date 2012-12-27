/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.chrome;

import edu.northwestern.bioinformatics.studycalendar.configuration.Configuration;
import edu.northwestern.bioinformatics.studycalendar.web.WebTestCase;

import static org.easymock.EasyMock.expect;

/**
 * @author Rhett Sutphin
 */
public class NewTemplateTaskTest extends WebTestCase {
    private NewTemplateTask task;
    private Configuration configuration;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        configuration = registerMockFor(Configuration.class);
        task = new NewTemplateTask();
        task.setConfiguration(configuration);
    }

    public void testEnabledWhenConfigurationOptionIsTrue() throws Exception {
        expect(configuration.get(Configuration.ENABLE_CREATING_TEMPLATE)).andReturn(true);

        replayMocks();
        assertTrue(task.isEnabled());
        verifyMocks();
    }

    public void testDisabledWhenConfigurationOptionIsFalse() throws Exception {
        expect(configuration.get(Configuration.ENABLE_CREATING_TEMPLATE)).andReturn(false);

        replayMocks();
        assertFalse(task.isEnabled());
        verifyMocks();
    }
}
