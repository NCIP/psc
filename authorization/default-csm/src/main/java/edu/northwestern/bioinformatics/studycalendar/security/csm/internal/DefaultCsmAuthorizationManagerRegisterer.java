/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.csm.internal;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Creates and registers the CSM authorization manager when the necessary
 * data source is available. If one is available on construction, it will use that.
 *
 * @author Rhett Sutphin
 */
public class DefaultCsmAuthorizationManagerRegisterer implements ServiceListener {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private static final String CSM_DATASOURCE_PID =
        "edu.northwestern.bioinformatics.studycalendar.database.CSM_DATASOURCE";

    private final BundleContext bundleContext;
    private final String pid;

    public DefaultCsmAuthorizationManagerRegisterer(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.pid = bundleContext.getBundle().getSymbolicName() + ".AUTHORIZATION_MANAGER";
    }

    public void start() {
        DataSource csmDataSource = lookUpCsmDataSource();
        if (csmDataSource == null) {
            log.debug("CSM DataSource not available yet; deferring CSM AuthorizationManager registration until it is");
            startListening();
        } else {
            log.debug("CSM DataSource available immediately; registering CSM AuthorizationManager");
            registerFor(csmDataSource);
        }
    }

    private void registerFor(DataSource csmDataSource) {
        bundleContext.registerService(
            AuthorizationManager.class.getName(),
            new DefaultCsmAuthorizationManagerFactory(csmDataSource).create(),
            new MapBuilder<String, Object>().
                put(Constants.SERVICE_RANKING, Integer.MIN_VALUE).
                put(Constants.SERVICE_PID, pid).
                toDictionary());
    }

    private void startListening() {
        try {
            bundleContext.addServiceListener(this,
                String.format("(&(%s=%s)(%s=%s))",
                    Constants.OBJECTCLASS, DataSource.class.getName(),
                    Constants.SERVICE_PID, CSM_DATASOURCE_PID));
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarError("Shouldn't happen: the syntax is valid", e);
        }
    }

    public void serviceChanged(ServiceEvent serviceEvent) {
        if (serviceEvent.getType() == ServiceEvent.REGISTERED) {
            log.debug("CSM DataSource seen to be registered; registering CSM AuthorizationManager");
            registerFor((DataSource) bundleContext.getService(serviceEvent.getServiceReference()));
        }
    }

    private DataSource lookUpCsmDataSource() {
        ServiceReference[] dsRefs;
        try {
            dsRefs = bundleContext.getServiceReferences(javax.sql.DataSource.class.getName(),
                String.format("(%s=%s)", Constants.SERVICE_PID, CSM_DATASOURCE_PID));
        } catch (InvalidSyntaxException e) {
            throw new StudyCalendarError("Shouldn't happen: the syntax is valid", e);
        }
        if (dsRefs == null || dsRefs.length == 0) {
            return null;
        } else {
            return (javax.sql.DataSource) bundleContext.getService(dsRefs[0]);
        }
    }
}
