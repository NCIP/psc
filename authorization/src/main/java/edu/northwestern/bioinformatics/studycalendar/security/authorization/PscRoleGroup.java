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
        "Subject Managment"
    ),
    ADMINISTRATION(
        "Administration"
    );

    private String description;

    private PscRoleGroup(String description) {
        this.description = description;
    }
}
