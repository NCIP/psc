package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

/**
 * @author Rhett Sutphin
 */
public enum StudyCalendarProtectionGroup {
    BASE("BaseAccess"),
    STUDY_COORDINATOR("CreateStudyAccess"),
    STUDY_ADMINISTRATOR("MarkTemplateCompleteAccess"), // TODO: change to AdministrativeAccess
    PARTICIPANT_COORDINATOR("ParticipantAssignmentAccess"),
    SITE_COORDINATOR("SiteCoordinatorAccess")
    ;

    private String csmProtectionGroupName;

    private StudyCalendarProtectionGroup(String csmName) {
        this.csmProtectionGroupName = csmName;
    }

    public String csmName() { return csmProtectionGroupName; }
}
