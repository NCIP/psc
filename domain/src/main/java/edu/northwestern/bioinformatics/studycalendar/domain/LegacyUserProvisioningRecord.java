/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.domain;

import org.apache.commons.lang.StringUtils;

import static java.lang.String.format;

public class LegacyUserProvisioningRecord {
    public static final String CSV_HEADER = StringUtils.join(
        new String[] { "Username", "First", "Last", "Is active?", "Role", "Site", "Study" }, ",");
    private String userName, firstName, lastName, siteName, studyName, role, active;

    public LegacyUserProvisioningRecord(String userName, String firstName, String lastName, String active, String role, String siteName, String studyName) {
        this.userName = userName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.active = active;
        this.role = role;
        this.siteName = siteName;
        this.studyName = studyName;
    }

    public String csv() {
        return format(
            "%s,%s,%s,%s,%s,%s,%s",
            nullAsBlank(userName),
            nullAsBlank(firstName),
            nullAsBlank(lastName),
            nullAsBlank(active),
            nullAsBlank(role),
            nullAsBlank(siteName),
            nullAsBlank(studyName)
        );
    }

    public String getUserName() {
        return userName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSiteName() {
        return siteName;
    }

    public String getStudyName() {
        return studyName;
    }

    public String getRole() {
        return role;
    }

    public String getActive() {
        return active;
    }

    protected String nullAsBlank(String s) {
        return (s != null) ? s : "";
    }
}