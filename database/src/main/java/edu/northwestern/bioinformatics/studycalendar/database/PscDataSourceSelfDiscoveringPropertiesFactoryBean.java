package edu.northwestern.bioinformatics.studycalendar.database;

import gov.nih.nci.cabig.ctms.tools.DataSourceSelfDiscoveringPropertiesFactoryBean;

/**
 * Note: the dynamic computation of the various authentication properties in here is
 * temporary for 1.5.  In 2.0, we will add a separate configuration system (design
 * TBD) for configuring a pluggable authentication & SSO module.
 *
 * @author Rhett Sutphin
 */
public class PscDataSourceSelfDiscoveringPropertiesFactoryBean extends DataSourceSelfDiscoveringPropertiesFactoryBean {
    private static final String DATASOURCE_SYSTEM_PROPERTY = "psc.config.datasource";

    private static final String AUTH_MODE_PROPERTY = "authenticationMode";
    private static final String WEBSSO_SERVER_URL_PROPERTY = "webSSO.server.baseUrl";
    private static final String WEBSSO_SERVER_CERT_PROPERTY = "webSSO.server.trustCert";
    private static final String WEBSSO_CAS_URL_PROPERTY = "webSSO.cas.acegi.security.url";

    //grid service related configuration
    private static final String GRID_REGISTRATION_CONSUMER_URL = "grid.registrationconsumer.url";

    private static final String GRID_REGISTRATION_CONSUMER_VALUE = "/wsrf-psc/services/cagrid/RegistrationConsumer";

    private static final String GRID_STUDY_CONSUMER_URL = "grid.studyconsumer.url";
    private static final String GRID_ROLLBACK_TIMEOUT = "grid.rollback.timeout";

    private static final String GRID_ROLLBACK_TIMEOUT_VALUE = "1";

    private static final String GRID_STUDY_CONSUMER_VALUE = "/wsrf-psc/services/cagrid/StudyConsumer";

    private static final String[] WEBSSO_PROPERTIES = {
            WEBSSO_CAS_URL_PROPERTY, WEBSSO_SERVER_URL_PROPERTY, WEBSSO_SERVER_CERT_PROPERTY
    };

    @Override
    public String getDatabaseConfigurationName() {
        String fromSys = System.getProperty(DATASOURCE_SYSTEM_PROPERTY);
        if (fromSys != null) {
            return fromSys;
        } else {
            return super.getDatabaseConfigurationName();
        }
    }

    @Override
    protected void computeProperties() {
        super.computeProperties();
        setPropertyDefault(AUTH_MODE_PROPERTY, "local");
        for (String webssoProperty : WEBSSO_PROPERTIES) {
            setPropertyDefault(webssoProperty, "If you are using CCTS-SSO, please set " + webssoProperty);
        }

        setPropertyDefault(GRID_STUDY_CONSUMER_URL, GRID_STUDY_CONSUMER_VALUE);
        setPropertyDefault(GRID_REGISTRATION_CONSUMER_URL, GRID_REGISTRATION_CONSUMER_VALUE);
        setPropertyDefault(GRID_ROLLBACK_TIMEOUT, GRID_ROLLBACK_TIMEOUT_VALUE);
    }

    private void setPropertyDefault(String propertyName, String defaultValue) {
        if (getProperties().getProperty(propertyName) == null) {
            getProperties().setProperty(propertyName, defaultValue);
        }
    }
}
