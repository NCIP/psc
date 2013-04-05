/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializerTestCase;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Rhett Sutphin
 */
public class RestfulApiInitializerIntegratedTest extends SchemaInitializerTestCase {
    private RestfulApiTestInitializer initializer;
    private DataSource datasource;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        datasource = (DataSource) getDeployedApplicationContext().getBean("nonPooledDataSource");
        initializer = new RestfulApiTestInitializer();
        initializer.setDataSource(datasource);
        initializer.setConfigurationInitializer(new ConfigurationInitializer());
        initializer.setSitesInitializer(new SitesInitializer());
        initializer.setUsersInitializer(new UsersInitializer());
        initializer.setSampleSourceInitializer(new SampleActivitySourceInitializer());
        assertFalse("Test setup failure: no initializers created",
            initializer.getInitializerSeries().isEmpty());
    }

    public void testRowPreservingInitializersForRealDatabaseUsePksThatExist() throws Exception {
        new JdbcTemplate(datasource).execute(new ConnectionCallback() {
            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                DatabaseMetaData metadata = con.getMetaData();
                for (SchemaInitializer schemaInitializer : initializer.getInitializerSeries()) {
                    if (schemaInitializer instanceof RowPreservingInitializer) {
                        RowPreservingInitializer rpInit = (RowPreservingInitializer) schemaInitializer;
                        Set<String> columns = collectColumns(metadata, rpInit.getTableName());
                        for (String pk : rpInit.getPrimaryKeyNames()) {
                            assertContains(
                                String.format("At least one of the PKs for %s doesn't exist", rpInit.getTableName()),
                                columns, pk.toLowerCase());
                        }
                    }
                }
                return null;
            }

            private Set<String> collectColumns(
                DatabaseMetaData metadata, String tableName
            ) throws SQLException {
                ResultSet cols = metadata.getColumns(null, null, tableName, null);
                if (!cols.isBeforeFirst()) {
                    cols = metadata.getColumns(null, null, tableName.toUpperCase(), null);
                }
                Set<String> set = new LinkedHashSet<String>();
                while (cols.next()) {
                    set.add(cols.getString("COLUMN_NAME").toLowerCase());
                }
                return set;
            }
        });
    }
}
