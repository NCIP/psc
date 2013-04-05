/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.security.authorization;

public enum PscRoleGroup {
    TEMPLATE_CREATION(
        "Template Creation"
    ),
    TEMPLATE_MANAGEMENT(
        "Template Management"
    ),
    SITE_MANAGEMENT(
        "Site Management"
    ),
    SUBJECT_MANAGEMENT(
        "Subject Management"
    ),
    ADMINISTRATION(
        "Administration"
    ),
    DATA_READER(
        "Data Reader"
    ),
    SUITE_ROLES(
        "Suite Roles"
    );

    private String displayName;

    private PscRoleGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getKey() {
      return toString().toLowerCase();
    }
}
