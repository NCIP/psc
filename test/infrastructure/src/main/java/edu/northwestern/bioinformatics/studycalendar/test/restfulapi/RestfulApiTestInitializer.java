/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.integrated.IntegratedTestDatabaseInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.RowPreservingInitializer;
import edu.northwestern.bioinformatics.studycalendar.test.integrated.SchemaInitializer;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.audit.domain.DataAuditInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class RestfulApiTestInitializer extends IntegratedTestDatabaseInitializer {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final Collection<String> NO_INITIALIZER_TABLES = Arrays.asList(
        "authentication_system_conf", "bering_version", "change_actions", "delta_node_types",
        RowPreservingInitializer.PK_RECORD_TABLE_NAME);
    private static final Map<String, List<String>> ALTERNATE_PK_TABLES
        = new MapBuilder<String, List<String>>().
            put("csm_user_pe", Arrays.asList("user_protection_element_id")).
            put("scheduled_activity_labels", Arrays.asList("scheduled_activity_id", "label")).
            put("subject_populations", Arrays.asList("assignment_id", "population_id")).
            put("user_role_sites", Arrays.asList("user_role_id", "site_id")).
            put("user_role_study_sites", Arrays.asList("user_role_id", "study_site_id")).
            put("osgi_cm_property_values", Arrays.asList("property_id", "list_index")).
            put("managing_sites", Arrays.asList("study_id", "site_id")).
            put("subject_properties", Arrays.asList("subject_id", "name")).
            toMap();

    private SitesInitializer sitesInitializer;
    private ConfigurationInitializer configurationInitializer;
    private UsersInitializer usersInitializer;
    private SampleActivitySourceInitializer sampleSourceInitializer;

    @Override
    public void oneTimeSetup() {
        initAuditInfo();
        super.oneTimeSetup();

        getJdbcTemplate().execute(new ConnectionCallback() {
            public Object doInConnection(Connection con) throws SQLException, DataAccessException {
                if (con.getMetaData().getDatabaseProductName().toLowerCase().contains("oracle")) {
                    log.debug("Purging Oracle recycle bin");
                    con.createStatement().execute("PURGE RECYCLEBIN");
                }
                return null;
            }
        });
    }

    @Override
    public void beforeAll() {
        initAuditInfo();
        super.beforeAll();
    }

    private void initAuditInfo() {
        DataAuditInfo.setLocal(new DataAuditInfo("restful-api-test", "none", new Date(), "[console]"));
    }

    @Override
    public SchemaInitializer getTableInitializer(String tableName) {
        tableName = tableName.toLowerCase();
        if ("configuration".equals(tableName)) {
            return configurationInitializer;
        } else if (tableName.equals(sitesInitializer.getTableName())) {
            return sitesInitializer;
        } else if (tableName.equals(usersInitializer.getTableName())) {
            return usersInitializer;
        } else if (tableName.equals(sampleSourceInitializer.getTableName())) {
            return sampleSourceInitializer;
        } else if (NO_INITIALIZER_TABLES.contains(tableName)) {
            return null;
        } else if (ALTERNATE_PK_TABLES.containsKey(tableName)) {
            return new RowPreservingInitializer(tableName, ALTERNATE_PK_TABLES.get(tableName));
        } else if (tableName.startsWith("csm_")) {
            String csmlessName = tableName.substring(4);
            return new RowPreservingInitializer(tableName, csmlessName + "_id");
        } else {
            return new RowPreservingInitializer(tableName);
        }
    }

    private JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(getDataSource());
    }

    ////// CONFIGURATION

    @Required
    public void setSitesInitializer(SitesInitializer sitesInitializer) {
        this.sitesInitializer = sitesInitializer;
    }

    @Required
    public void setConfigurationInitializer(ConfigurationInitializer configurationInitializer) {
        this.configurationInitializer = configurationInitializer;
    }

    @Required
    public void setUsersInitializer(UsersInitializer usersInitializer) {
        this.usersInitializer = usersInitializer;
    }

    @Required
    public void setSampleSourceInitializer(SampleActivitySourceInitializer sampleSourceInitializer) {
        this.sampleSourceInitializer = sampleSourceInitializer;
    }
}
