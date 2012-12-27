/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserTemplateRelationship;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * An enum of all the study privilege mapping used by study resource in the system.
 * @author Jalpa Patel
 */
public enum StudyPrivilege {
    AMEND("canStartAmendment"),
    DEVELOP("canDevelop"),
    SEE_DEVELOPMENT("canSeeDevelopmentVersion"),
    SET_MANAGING_SITES("canChangeManagingSites"),
    RELEASE("canRelease"),
    SET_PARTICIPATION("canSetParticipation"),
    APPROVE("canApprove"),
    SCHEDULE_RECONSENT("canScheduleReconsent"),
    REGISTER("canAssignSubjects"),
    SEE_RELEASED("canSeeReleasedVersions"),
    ASSIGN_IDENTIFIERS("canAssignIdentifiers"),
    PURGE("canPurge")
    ;

    private String propertyName;

    StudyPrivilege(String propertyName) {
        this.propertyName = propertyName;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public static List<StudyPrivilege> valuesFor(UserTemplateRelationship utr) {
        BeanWrapper bw = new BeanWrapperImpl(utr);
        List<StudyPrivilege> privileges = new ArrayList<StudyPrivilege>();

        for (StudyPrivilege privilege : StudyPrivilege.values()) {
           if (((Boolean)bw.getPropertyValue(privilege.getPropertyName()))) {
              privileges.add(privilege);
           }
        }
        return privileges;
    }
    
    public static StudyPrivilege lookUp(String str) {
        StudyPrivilege[] privileges = StudyPrivilege.values();
        for (StudyPrivilege privilege : privileges) {
            if (str.equals(privilege.attributeName())){
                String name = str.replaceAll("-","_").toUpperCase();
                return StudyPrivilege.valueOf(name);
            }
        }
        return null;
    }
}
