/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.easymock.classextension.EasyMock.*;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationInitializerTest extends SchemaInitializerTestCase {
    public void testDoesUpdateInSetupIfRecordAlreadyExists() throws Exception {
        expectPropQuery("configuration", "deploymentName");
        expect(jdbc.update("UPDATE configuration SET value=? WHERE prop=?",
            "hot-cha", "deploymentName")).andReturn(1);

        replayMocks();
        String yaml = "configuration:\n  deploymentName: hot-cha\n";
        ConfigurationInitializer init = createInitializer(yaml);
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testDoesInsertInSetupIfRecordDoesNotExist() throws Exception {
        expectPropQuery("configuration");
        expect(jdbc.update("INSERT INTO configuration (prop, value) VALUES (?, ?)",
            "deploymentName", "hot-cha")).andReturn(1);

        replayMocks();
        ConfigurationInitializer init = createInitializer("configuration:\n  deploymentName: hot-cha\n");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testDeletesUnreferencedPropsInSetup() throws Exception {
        expectPropQuery("configuration", "deploymentName", "other", "polite");
        expect(jdbc.update("UPDATE configuration SET value=? WHERE prop=?",
            "hot-cha", "deploymentName")).andReturn(1);
        expect(jdbc.update("DELETE FROM configuration WHERE prop=? OR prop=?",
            "other", "polite")).andReturn(2);

        replayMocks();
        ConfigurationInitializer init
            = createInitializer("configuration:\n  deploymentName: hot-cha\n");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testHandlesConfigurationForMultipleTables() throws Exception {
        expectPropQuery("configuration", "deploymentName");
        expectPropQuery("alt_conf");
        expect(jdbc.update("UPDATE configuration SET value=? WHERE prop=?",
            "hot-cha", "deploymentName")).andReturn(1);
        expect(jdbc.update("INSERT INTO alt_conf (prop, value) VALUES (?, ?)",
            "deploymentName", "tries")).andReturn(1);

        replayMocks();
        ConfigurationInitializer init
            = createInitializer("configuration:\n  deploymentName: hot-cha\nalt_conf:\n  deploymentName: tries");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    private ConfigurationInitializer createInitializer(String yaml) throws Exception {
        ConfigurationInitializer initializer = new ConfigurationInitializer();
        initializer.setYamlResource(literalYamlResource(yaml));
        initializer.afterPropertiesSet();
        return initializer;
    }

    private void expectPropQuery(String tableName, String... expectedProps) {
        List<Map<String, Object>> expectedResult = new ArrayList<Map<String, Object>>(expectedProps.length);
        for (String expectedProp : expectedProps) {
            expectedResult.add(Collections.<String, Object>singletonMap("prop", expectedProp));
        }
        expect(jdbc.queryForList("SELECT prop FROM " + tableName)).andReturn(expectedResult);
    }
}
