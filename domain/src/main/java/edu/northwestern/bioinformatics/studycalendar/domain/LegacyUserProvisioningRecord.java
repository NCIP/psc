package edu.northwestern.bioinformatics.studycalendar.domain;

import static java.lang.String.format;

public class LegacyUserProvisioningRecord {
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
        return format("%s,%s,%s,%s,%s,%s,%s", userName, firstName, lastName, active, role, siteName, studyName);
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
}