package edu.northwestern.bioinformatics.studycalendar.core.accesscontrol;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import gov.nih.nci.cabig.ctms.suite.authorization.ScopeType;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembershipLoader;
import org.springframework.beans.factory.annotation.Required;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Delegates role lookups to the OSGi-advertised version of {@link SuiteRoleMembershipLoader}.
 * Since the OSGi one does not have access to the study and site mappings, it recreates the
 * {@link SuiteRoleMembership}s as they pass through with references to those mappings.
 *
 * @author Rhett Sutphin
 */
public class OsgiSuiteRoleMembershipLoader extends SuiteRoleMembershipLoader {
    private OsgiLayerTools osgiLayerTools;

    @Override
    public Map<SuiteRole, SuiteRoleMembership> getRoleMemberships(long userId) {
        return translate(getOsgiLayerTools().getRequiredService(SuiteRoleMembershipLoader.class).
            getRoleMemberships(userId));
    }

    @Override
    public Map<SuiteRole, SuiteRoleMembership> getProvisioningRoleMemberships(long userId) {
        return translate(getOsgiLayerTools().getRequiredService(SuiteRoleMembershipLoader.class).
            getProvisioningRoleMemberships(userId));
    }

    private Map<SuiteRole, SuiteRoleMembership> translate(
        Map<SuiteRole, SuiteRoleMembership> memberships
    ) {
        Map<SuiteRole, SuiteRoleMembership> translated = new LinkedHashMap<SuiteRole, SuiteRoleMembership>();
        for (SuiteRoleMembership membership : memberships.values()) {
            translated.put(membership.getRole(), translate(membership));
        }
        return translated;
    }

    private SuiteRoleMembership translate(SuiteRoleMembership membership) {
        SuiteRoleMembership translated =
            new SuiteRoleMembership(membership.getRole(), getSiteMapping(), getStudyMapping());
        for (ScopeType type : ScopeType.values()) {
            if (membership.isAll(type)) {
                translated.forAll(type);
            } else {
                List<String> identifiers = membership.getIdentifiers(type);
                translated.forIdentifiers(type, identifiers.toArray(new String[identifiers.size()]));
            }
        }
        return translated;
    }

    protected OsgiLayerTools getOsgiLayerTools() {
        return osgiLayerTools;
    }

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }
}
