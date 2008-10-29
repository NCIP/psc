package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import static org.easymock.classextension.EasyMock.*;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Rhett Sutphin
 */
public class ConfigurationInitializerTest extends SchemaInitializerTestCase {
    public void testDoesUpdateInSetupIfRecordAlreadyExists() throws Exception {
        expect(jdbc.queryForList("SELECT prop FROM configuration"))
            .andReturn(Arrays.asList("deploymentName"));
        expect(jdbc.update(eq("UPDATE configuration SET value=? WHERE prop=?"),
            aryEq(new Object[] { "hot-cha", "deploymentName" }))).andReturn(1);

        replayMocks();
        String yaml = "configuration:\n  deploymentName: hot-cha\n";
        ConfigurationInitializer init = createInitializer(yaml);
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testDoesInsertInSetupIfRecordDoesNotExist() throws Exception {
        expect(jdbc.queryForList("SELECT prop FROM configuration")).andReturn(Collections.emptyList());
        expect(jdbc.update(eq("INSERT INTO configuration (prop, value) VALUES (?, ?)"),
            aryEq(new Object[] { "deploymentName", "hot-cha" }))).andReturn(1);

        replayMocks();
        ConfigurationInitializer init = createInitializer("configuration:\n  deploymentName: hot-cha\n");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testDeletesUnreferencedPropsInSetup() throws Exception {
        expect(jdbc.queryForList("SELECT prop FROM configuration")).
            andReturn(Arrays.asList("deploymentName", "other", "polite"));
        expect(jdbc.update(eq("UPDATE configuration SET value=? WHERE prop=?"),
            aryEq(new Object[] { "hot-cha", "deploymentName" }))).andReturn(1);
        expect(jdbc.update(eq("DELETE FROM configuration WHERE prop=? OR prop=?"),
            aryEq(new Object[] { "other", "polite" }))).andReturn(2);

        replayMocks();
        ConfigurationInitializer init
            = createInitializer("configuration:\n  deploymentName: hot-cha\n");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    public void testHandlesConfigurationForMultipleTables() throws Exception {
        expect(jdbc.queryForList("SELECT prop FROM configuration")).
            andReturn(Arrays.asList("deploymentName"));
        expect(jdbc.queryForList("SELECT prop FROM alt_conf")).
            andReturn(Collections.emptyList());
        expect(jdbc.update(eq("UPDATE configuration SET value=? WHERE prop=?"),
            aryEq(new Object[] { "hot-cha", "deploymentName" }))).andReturn(1);
        expect(jdbc.update(eq("INSERT INTO alt_conf (prop, value) VALUES (?, ?)"),
            aryEq(new Object[] { "deploymentName", "tries" }))).andReturn(1);

        replayMocks();
        ConfigurationInitializer init
            = createInitializer("configuration:\n  deploymentName: hot-cha\nalt_conf:\n  deploymentName: tries");
        init.oneTimeSetup(connectionSource);
        verifyMocks();
    }

    private ConfigurationInitializer createInitializer(String yaml) throws Exception {
        ConfigurationInitializer initializer = new ConfigurationInitializer();
        initializer.setData(literalYamlResource(yaml));
        initializer.afterPropertiesSet();
        return initializer;
    }
}
