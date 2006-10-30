package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

/**
 * @author Rhett Sutphin
 */
public enum StudyCalendarProtectionGroup {
    BASE("BaseAccess"),
    STUDY_COORDINATOR("CreateStudyAccess"),
    // Note that this is the spelling (missing 'e') in the protection group
    // TODO: change to AdministrativeAccess
    STUDY_ADMINISTRATOR("MarkTemplatCompleteAccess"),
    PARTICIPANT_COORDINATOR("ParticipantAssignmentAccess"),
    SITE_COORDINATOR("SiteCoordinatorAccess")
    ;

    private String csmProtectionGroupName;

    private StudyCalendarProtectionGroup(String csmName) {
        this.csmProtectionGroupName = csmName;
    }

    public String csmName() { return csmProtectionGroupName; }
}
