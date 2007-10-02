package edu.northwestern.bioinformatics.studycalendar.domain;

import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.OrderBy;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import gov.nih.nci.security.util.StringEncrypter;

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
    private Set<Role> roles = new HashSet<Role>();
    private Boolean activeFlag;
    private String password;
    private List<StudyParticipantAssignment> studyParticipantAssignments = new ArrayList<StudyParticipantAssignment>();

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

    public String getPassword() throws Exception {
        return password;
    }

    public void setPassword(String password) throws Exception {
        this.password = password;
    }

    @Transient
    public String getPlainTextPassword() throws Exception{
        StringEncrypter encrypter = new StringEncrypter();
        return encrypter.decrypt(password);
    }

    @Transient
    public void setPlainTextPassword(String password) throws Exception {
        StringEncrypter encrypter = new StringEncrypter();
        this.password = encrypter.encrypt(password);
    }

    @CollectionOfElements
    @Type(type = "userRole")
    @JoinTable(
        name="user_roles",
        joinColumns = @JoinColumn(name="user_id")
    )
    @Column(name="csm_group_name")
    public Set<Role> getRoles() {
        return roles;
    } 

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
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

        if (name != null ? !name.equals(user.name) : user.name != null) return false;
        if (csmUserId != null ?
                !csmUserId.equals(user.csmUserId) : user.csmUserId != null) return false;
        if (activeFlag != null ? !activeFlag.equals(user.activeFlag) : user.activeFlag != null) return false;
        if (password != null ? !password.equals(user.password) : user.password != null) return false;
        if (roles.size() != user.getRoles().size()) return false;
        return !(roles != null ? !roles.equals(user.getRoles()) : user.getRoles() != null);

    }

    public int hashCode() {
        return (name != null ? name.hashCode() : 0);
    }
}
