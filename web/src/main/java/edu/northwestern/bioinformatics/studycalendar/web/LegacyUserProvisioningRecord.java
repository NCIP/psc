package edu.northwestern.bioinformatics.studycalendar.web;

import static java.lang.String.format;

public class LegacyUserProvisioningRecord {
    String username, firstName, lastName, siteName, studyName, role, active;

    public LegacyUserProvisioningRecord(String username, String firstName, String lastName, String siteName, String studyName, String role, String active) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.siteName = siteName;
        this.studyName = studyName;
        this.role = role;
        this.active = active;
    }

    public String csv() {
        return format("%s,%s,%s,%s,%s,%s,%s", username, firstName, lastName, siteName, studyName, role, active);
    }
}