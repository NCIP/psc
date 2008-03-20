package edu.northwestern.bioinformatics.studycalendar.tools;

import gov.nih.nci.cabig.ctms.tools.DataSourceSelfDiscoveringPropertiesFactoryBean;

/**
 * Note: the dynamic computation of the various authentication properties in here is
 * temporary for 1.5.  In 2.0, we will add a separate configuration system (design
 * TBD) for configuring a pluggable authentication & SSO module.
 *
 * @author Rhett Sutphin
 */
public class PscDataSourceSelfDiscoveringPropertiesFactoryBean extends DataSourceSelfDiscoveringPropertiesFactoryBean {
    private static final String AUTH_MODE_PROPERTY = "authenticationMode";
    private static final String WEBSSO_SERVER_URL_PROPERTY = "webSSO.server.baseUrl";
    private static final String WEBSSO_SERVER_CERT_PROPERTY = "webSSO.server.trustCert";
    private static final String WEBSSO_CAS_URL_PROPERTY = "webSSO.cas.acegi.security.url";
    private static final String[] WEBSSO_PROPERTIES = {
        WEBSSO_CAS_URL_PROPERTY, WEBSSO_SERVER_URL_PROPERTY, WEBSSO_SERVER_CERT_PROPERTY
    };

    @Override
    protected void computeProperties() {
        super.computeProperties();
        setPropertyDefault(AUTH_MODE_PROPERTY, "local");
        for (String webssoProperty : WEBSSO_PROPERTIES) {
            setPropertyDefault(webssoProperty, "If you are using CCTS-SSO, please set " + webssoProperty);
        }
    }

    private void setPropertyDefault(String propertyName, String defaultValue) {
        if (getProperties().getProperty(propertyName) == null) {
            getProperties().setProperty(propertyName, defaultValue);
        }
    }
}
