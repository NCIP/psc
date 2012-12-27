/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration of the large-scale sorts of uses to which PscRoles apply.
 *
 * @see edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole#getUses()
 * @author Rhett Sutphin
 */
public enum PscRoleUse {
    TEMPLATE_MANAGEMENT,
    SITE_PARTICIPATION,
    SUBJECT_MANAGEMENT,
    ADMINISTRATION,
    ACCESSORY,
    GRID_SERVICES
    ;

    private PscRole[] roles;

    /**
     * Returns the roles which have this use.
     * @see PscRole#getUses()
     */
    public synchronized PscRole[] roles() {
        if (roles == null) {
            List<PscRole> uses = new ArrayList<PscRole>(PscRole.values().length);
            for (PscRole role : PscRole.values()) {
                if (role.getUses().contains(this)) uses.add(role);
            }
            this.roles = uses.toArray(new PscRole[uses.size()]);
        }
        return roles;
    }
}
