/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.authorization.auxiliary.internal;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import gov.nih.nci.security.AuthorizationManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

import java.util.Comparator;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

public class SuiteRoleMembershipLoaderRegisterer {
    private BundleContext bundleContext;
    private SortedSet<ServiceReference> authorizationManagerReferences;
    private ServiceReference currentSource;
    private ServiceRegistration current;

    protected void activate(BundleContext bc) {
        this.bundleContext = bc;
        this.authorizationManagerReferences =
            new ConcurrentSkipListSet<ServiceReference>(new ReferenceByRankingAndId());
        this.current = null;
        this.currentSource = null;
    }

    protected synchronized void createLoader(ServiceReference reference) {
        authorizationManagerReferences.add(reference);
        updateCurrentSuiteRoleMembershipLoader();
    }

    private void updateCurrentSuiteRoleMembershipLoader() {
        if (current != null && authorizationManagerReferences.isEmpty()) {
            current.unregister();
        } else if (currentSource != authorizationManagerReferences.first()) {
            registerSuiteRoleMembershipLoaderFor(authorizationManagerReferences.first());
        }
    }

    private void registerSuiteRoleMembershipLoaderFor(
        ServiceReference authorizationManagerReference
    ) {
        AuthorizationManager newAuthorizationManger =
            (AuthorizationManager) bundleContext.getService(authorizationManagerReference);
        SuiteRoleMembershipLoader newLoader = new SuiteRoleMembershipLoader();
        newLoader.setAuthorizationManager(newAuthorizationManger);

        if (current != null) {
            current.unregister();
        }
        String sourcePid = (String) authorizationManagerReference.getProperty(Constants.SERVICE_PID);
        current = bundleContext.registerService(
            SuiteRoleMembershipLoader.class.getName(), newLoader,
            new MapBuilder<String, Object>().
                put("authorizationManagerService", sourcePid).
                put(Constants.SERVICE_PID, "SuiteRoleMembershipLoader for " + sourcePid).
                toDictionary());
        currentSource = authorizationManagerReference;
    }

    protected synchronized void changeLoader(ServiceReference reference) {
        authorizationManagerReferences.remove(reference);
        updateCurrentSuiteRoleMembershipLoader();
    }

    private class ReferenceByRankingAndId implements Comparator<ServiceReference> {
        public int compare(ServiceReference a, ServiceReference b) {
            int rankComparison = rank(b) - rank(a);
            if (rankComparison != 0) return rankComparison;

            return id(b) - id(a);
        }

        private int id(ServiceReference reference) {
            return extractIntegerProperty(Constants.SERVICE_ID, reference);
        }

        private int rank(ServiceReference reference) {
            return extractIntegerProperty(Constants.SERVICE_RANKING, reference);
        }

        private int extractIntegerProperty(String key, ServiceReference reference) {
            Number rank = (Number) reference.getProperty(key);
            if (rank == null) {
                return 0;
            } else {
                return rank.intValue();
            }
        }
    }
}
