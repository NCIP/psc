package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.security.util.StringEncrypter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.apache.commons.lang.StringUtils;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table (name = "users")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_users_id")
    }
)

public class User extends AbstractMutableDomainObject implements Named {
    private String name;
    private Long csmUserId;
    private Set<UserRole> userRoles;
    private Boolean activeFlag;
    private List<StudyParticipantAssignment> studyParticipantAssignments;


    public User() {
        this.userRoles = new HashSet<UserRole>();
        this.studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();
        this.activeFlag = true;
    }

    public String getName() {
        return name;
    }

    public Long getCsmUserId() {
        return csmUserId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCsmUserId(Long csmUserId) {
        this.csmUserId = csmUserId;
    }

    public Boolean getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(Boolean activeFlag) {
        this.activeFlag = activeFlag;
    }

    @OneToMany (mappedBy = "user")
    @Cascade( {CascadeType.ALL, CascadeType.DELETE_ORPHAN})
    public Set<UserRole> getUserRoles() {
        return userRoles;
    } 

    public void setUserRoles(Set<UserRole> userRoles) {
        this.userRoles = userRoles;
    }

    public void addUserRole(UserRole userRole) {
        userRoles.add(userRole);
    }

    public void removeUserRole(UserRole userRole) {
        userRoles.remove(userRole);
    }

    public void clearUserRoles() {
        userRoles.clear();
    }

    public void addAllUserRoles(Set<UserRole> userRoles) {
        this.userRoles.addAll(userRoles);
    }

    @Transient
    public String getDisplayName() {
        return name;
    }
    
    @OneToMany (mappedBy = "participantCoordinator")
    @OrderBy // order by ID for testing consistency
    @Cascade (value = { org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN })
    public List<StudyParticipantAssignment> getStudyParticipantAssignments() {
        return studyParticipantAssignments;
    }

    public void setStudyParticipantAssignments(List<StudyParticipantAssignment> studyParticipantAssignments) {
        this.studyParticipantAssignments = studyParticipantAssignments;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return !(name != null ? !name.equals(user.name) : user.name != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}
