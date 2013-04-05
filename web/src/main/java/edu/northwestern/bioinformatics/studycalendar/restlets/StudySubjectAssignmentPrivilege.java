/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.service.presenter.UserStudySubjectAssignmentRelationship;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jalpa Patel
 */
public enum StudySubjectAssignmentPrivilege {
    VISIBLE("visible"),
    UPDATE_SCHEDULE("canUpdateSchedule"),
    CALENDAR_MANAGER("calendarManager"),
    SET_CALENDAR_MANAGER("canSetCalendarManager")
    ;

    private String propertyName;

    StudySubjectAssignmentPrivilege(String propertyName) {
        this.propertyName = propertyName;
    }

    public String attributeName() {
        return name().replaceAll("_", "-").toLowerCase();
    }

    public String getPropertyName() {
        return propertyName;
    }

    public static List<StudySubjectAssignmentPrivilege> valuesFor(UserStudySubjectAssignmentRelationship ussar) {
        BeanWrapper bw = new BeanWrapperImpl(ussar);
        List<StudySubjectAssignmentPrivilege> privileges = new ArrayList<StudySubjectAssignmentPrivilege>();

        for (StudySubjectAssignmentPrivilege privilege : StudySubjectAssignmentPrivilege.values()) {

           if (((Boolean)bw.getPropertyValue(privilege.getPropertyName()))) {
              privileges.add(privilege);
           }
        }
        return privileges;
    }

    public static StudySubjectAssignmentPrivilege lookUp(String str) {
        StudySubjectAssignmentPrivilege[] privileges = StudySubjectAssignmentPrivilege.values();
        for (StudySubjectAssignmentPrivilege privilege : privileges) {
            if (str.equals(privilege.attributeName())){
                String name = str.replaceAll("-","_").toUpperCase();
                return StudySubjectAssignmentPrivilege.valueOf(name);
            }
        }
        return null;
    }
}

