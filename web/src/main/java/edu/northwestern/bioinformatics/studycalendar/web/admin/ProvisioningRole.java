/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarError;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

import static java.util.Arrays.asList;

/**
* @author Rhett Sutphin
*/
public class ProvisioningRole implements Comparable<ProvisioningRole> {
    private SuiteRole suiteRole;
    private PscRole pscRole;

    public ProvisioningRole(SuiteRole suiteRole) {
        this.suiteRole = suiteRole;
        this.pscRole = PscRole.valueOf(suiteRole);
    }

    public boolean isPscRole() {
        return pscRole != null;
    }

    public String getDisplayName() {
        return suiteRole.getDisplayName();
    }

    public String getDescription() {
        if (isPscRole()) {
            return pscRole.getDescription();
        } else {
            return suiteRole.getDescription();
        }
    }

    public String getKey() {
        return suiteRole.getCsmName();
    }
    
    // for JSP-EL
    public String getJson() {
        try {
            return toJSON().toString(4);
        } catch (JSONException e) {
            throw new StudyCalendarSystemException("Could not stringify JSON", e);
        }
    }

    public JSONObject toJSON() {
        JSONObject o = new JSONObject();
        safePut(o, "name", suiteRole.getDisplayName());
        safePut(o, "key", suiteRole.getCsmName());
        safePut(o, "description", getDescription());
        safePut(o, "isPscRole", isPscRole());
        if (suiteRole.isScoped()) {
            JSONArray scopes = buildEnumArray(suiteRole.getScopes());
            safePut(o, "scopes", scopes);
        }
        if (pscRole != null) {
            if (!pscRole.getUses().isEmpty()) {
                JSONArray uses = buildEnumArray(pscRole.getUses());
                safePut(o, "uses", uses);
            }
            safePut(o, "scope_description", pscRole.getScopeDescription());
            if (asList(PscRole.valuesProvisionableByStudyTeamAdministrator()).contains(pscRole)) {
                safePut(o, "study_team_member", true);
            }
        }
        return o;
    }

    private JSONArray buildEnumArray(Collection<? extends Enum<?>> values) {
        JSONArray a = new JSONArray();
        for (Enum<?> use : values) {
            a.put(use.name().toLowerCase());
        }
        return a;
    }

    private void safePut(JSONObject target, String key, Object value) {
        try {
            target.put(key, value);
        } catch (JSONException e) {
            throw new StudyCalendarError("Adding %s: %s to %s unexpectedly failed", e, key, value, target);
        }
    }

    ////// COMPARABLE

    public int compareTo(ProvisioningRole other) {
        return PscRole.ORDER.compare(this.suiteRole, other.suiteRole);
    }

    ////// OBJECT METHODS

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProvisioningRole that = (ProvisioningRole) o;

        return suiteRole == that.suiteRole;
    }

    @Override
    public int hashCode() {
        return suiteRole != null ? suiteRole.hashCode() : 0;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append('[').append(suiteRole).append(']').
            toString();
    }
}
