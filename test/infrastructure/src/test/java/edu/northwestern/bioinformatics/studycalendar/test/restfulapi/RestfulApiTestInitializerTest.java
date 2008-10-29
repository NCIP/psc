package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.MockDbMetadata;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializer;
import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
import static org.easymock.classextension.EasyMock.expect;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rhett Sutphin
 */
public class RestfulApiTestInitializerTest extends StudyCalendarTestCase {
    private RestfulApiTestInitializer initializer;
    private MockDbMetadata metadata;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        metadata = new MockDbMetadata();

        DataSource dataSource = registerNiceMockFor(DataSource.class);
        Connection connection = registerNiceMockFor(Connection.class);
        expect(dataSource.getConnection()).andStubReturn(connection);
        expect(connection.getMetaData()).andStubReturn(metadata);
        replayMocks();

        initializer = new RestfulApiTestInitializer();
        initializer.setDataSource(dataSource);
    }

    public void testSitesTableHandledByInjectedSiteInitializer() throws Exception {
        metadata.solo("sites");

        SitesInitializer expected = new SitesInitializer();
        initializer.setSitesInitializer(expected);

        assertEquals("Wrong number of initializers", 1, initializer.getInitializerSeries().size());
        assertSame("Initializer is not the injected one", expected, initializer.getInitializerSeries().get(0));
    }

    public void testConfigurationsHandledByInjectedConfigurationInitializer() throws Exception {
        metadata.solo("configuration");
        metadata.solo("authentication_system_conf");

        ConfigurationInitializer expected = new ConfigurationInitializer();
        initializer.setConfigurationInitializer(expected);

        assertEquals("Wrong number of initializers", 1, initializer.getInitializerSeries().size());
        assertSame("Initializer is not the injected one", expected, initializer.getInitializerSeries().get(0));
    }

    public void testUsersAndRolesHandledByUserInitializer() throws Exception {
        metadata.link("users", "user_roles");
        metadata.link("user_roles", "user_role_sites", "user_role_study_sites");

        UsersInitializer expected = new UsersInitializer();
        initializer.setUsersInitializer(expected);

        assertEquals("Wrong number of initializers", 4, initializer.getInitializerSeries().size());
        assertRowPreservingInitializer("users", "id", initializer.getInitializerSeries().get(0));
        assertRowPreservingInitializer("user_roles", "id", initializer.getInitializerSeries().get(1));
        assertRowPreservingInitializer("user_role_sites", Arrays.asList("user_role_id", "site_id"), initializer.getInitializerSeries().get(2));
        assertSame("user_role_sites should be handled by injected UsersInitializer", expected, initializer.getInitializerSeries().get(2));
        assertRowPreservingInitializer("user_role_study_sites",
            Arrays.asList("user_role_id", "study_site_id"), initializer.getInitializerSeries().get(3));
    }

    public void testCsmTablesAreHandledByRowPreservingInitializer() throws Exception {
        metadata.link("csm_application", "csm_group");
        metadata.link("csm_group", "csm_user");
        List<SchemaInitializer> actualInitializers = initializer.getInitializerSeries();
        System.out.println(actualInitializers);
        assertEquals("Wrong number of initializers", 3, actualInitializers.size());
        assertRowPreservingInitializer("csm_application", "application_id", actualInitializers.get(0));
        assertRowPreservingInitializer("csm_group", "group_id", actualInitializers.get(1));
        assertRowPreservingInitializer("csm_user", "user_id", actualInitializers.get(2));
    }

    public void testNoInitializerCreatedForBeringVersion() throws Exception {
        metadata.solo("bering_version");
        List<SchemaInitializer> actualInitializers = initializer.getInitializerSeries();
        assertEquals("Wrong number of initializers", 0, actualInitializers.size());
    }

    private static void assertRowPreservingInitializer(String expectedTable, String expectedPk, SchemaInitializer actual) {
        assertRowPreservingInitializer(expectedTable, Arrays.asList(expectedPk), actual);
    }

    private static void assertRowPreservingInitializer(String expectedTable, List<String> expectedPks, SchemaInitializer actual) {
        assertTrue("Initializer is wrong type", actual instanceof RowPreservingInitializer);
        assertEquals("Initializer is for wrong table", expectedTable,
            ((RowPreservingInitializer) actual).getTableName());
        assertEquals("Initializer has wrong PK", expectedPks,
            ((RowPreservingInitializer) actual).getPrimaryKeyNames());
    }
}
