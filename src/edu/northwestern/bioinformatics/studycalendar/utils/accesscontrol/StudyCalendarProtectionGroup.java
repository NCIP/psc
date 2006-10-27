package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

/**
 * @author Rhett Sutphin
 */
public enum StudyCalendarProtectionGroup {
    BASE("BaseAccess"),
    CREATE_STUDY("CreateStudyAccess"),
    ADMIN("MarkTemplateCompleteAccess"), // TODO: change to AdministrativeAccess
    ASSIGN_PARTICIPANT("ParticipantAssignmentAccess"),
    SITE_COORDINATOR("SiteCoordinatorAccess")
    ;

    private String csmProtectionGroupName;

    private StudyCalendarProtectionGroup(String csmName) {
        this.csmProtectionGroupName = csmName;
    }

    public String csmName() { return csmProtectionGroupName; }
}
