package edu.northwestern.bioinformatics.studycalendar.test.restfulapi;

import edu.northwestern.bioinformatics.studycalendar.test.ConnectionSource;
import edu.northwestern.bioinformatics.studycalendar.test.EmptySchemaInitializer;
import org.apache.commons.lang.StringUtils;
import org.jvyaml.YAML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Ensures (in {@link #oneTimeSetup} that the configuration tables mirror the values stored in
 * an external YAML file (and provided to this class deserialized).
 *
 * @author Rhett Sutphin
 */
public class ConfigurationInitializer extends EmptySchemaInitializer implements InitializingBean {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private Map<String, Object> configurations;
    private Resource data;

    @Override
    public void oneTimeSetup(ConnectionSource connectionSource) {
        for (String table : configurations.keySet()) {
            oneTimeSetup(connectionSource, table);
        }
    }

    @SuppressWarnings({ "unchecked" })
    private void oneTimeSetup(ConnectionSource connectionSource, String table) {
        Map<String, String> desiredConfigProps = (Map<String, String>) configurations.get(table);
        List<String> existingProps = new ArrayList<String>(
            connectionSource.currentJdbcTemplate().queryForList("SELECT prop FROM " + table));
        for (String prop : desiredConfigProps.keySet()) {
            String desiredValue = desiredConfigProps.get(prop);
            if (existingProps.contains(prop)) {
                log.debug("Already a record for {} in {}; updating", prop, table);
                connectionSource.currentJdbcTemplate().update(
                    String.format("UPDATE %s SET value=? WHERE prop=?", table),
                    new Object[] { desiredValue, prop }
                );
                existingProps.remove(prop);
            } else {
                log.debug("No record for {} in {}; inserting", prop, table);
                connectionSource.currentJdbcTemplate().update(
                    String.format("INSERT INTO %s (prop, value) VALUES (?, ?)", table),
                    new Object[] { prop, desiredValue }
                );
            }
        }
        // remaining contents of existingProps are not referenced in the config data, so should be deleted
        deleteConfigurationProperties(connectionSource, table, existingProps);
    }

    private void deleteConfigurationProperties(ConnectionSource connectionSource, String table, List<String> propsToDelete) {
        if (!propsToDelete.isEmpty()) {
            log.debug("Deleting extra props {} from {}", propsToDelete, table);
            String[] deleteConds = new String[propsToDelete.size()];
            for (int i = 0; i < deleteConds.length; i++) {
                deleteConds[i] = "prop=?";
            }
            connectionSource.currentJdbcTemplate().update(
                String.format("DELETE FROM %s WHERE %s", table, StringUtils.join(deleteConds, " OR ")),
                propsToDelete.toArray(new Object[propsToDelete.size()]));
        }
    }

    ////// CONFIGURATION

    public void setData(Resource data) {
        this.data = data;
    }

    @SuppressWarnings({ "unchecked" })
    public void afterPropertiesSet() throws Exception {
        configurations = (Map<String, Object>) YAML.load(new InputStreamReader(data.getInputStream()));
    }
}
