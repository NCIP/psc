package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

/**
 * @author Rhett Sutphin
 */
public enum StudyCalendarProtectionGroup {
    STUDY_COORDINATOR("CreateStudyAccess"),
    STUDY_ADMINISTRATOR("AdministrativeAccess"),
    SUBJECT_COORDINATOR("SubjectAssignmentAccess"),
    SITE_COORDINATOR("SiteCoordinatorAccess")
    ;

    private String csmProtectionGroupName;

    private StudyCalendarProtectionGroup(String csmName) {
        this.csmProtectionGroupName = csmName;
    }

    public String csmName() { return csmProtectionGroupName; }
}
