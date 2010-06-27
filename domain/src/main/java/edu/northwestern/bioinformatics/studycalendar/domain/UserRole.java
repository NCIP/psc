package edu.northwestern.bioinformatics.studycalendar.domain;

import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "user_roles")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_user_roles_id")
    }
)
@Deprecated
public class UserRole  extends AbstractMutableDomainObject implements Serializable {
    private User user;
    private Role role;
    // TODO: why is one of these a set and the other a list?
    private Set<Site> sites = new LinkedHashSet<Site>();
    private List<StudySite> studySites = new ArrayList<StudySite>();

    public UserRole() { }

    public UserRole(User user, Role role) {
        this.user = user;
        this.role = role;
    }

    public UserRole(User user, Role role, Site... sites) {
        this.user = user;
        this.role = role;

        for (Site site: sites) {
            addSite(site);
        }
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Type(type = "userRole")
    @Column(name="csm_group_name")
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    @OneToMany
    @JoinTable( name="user_role_sites",
        joinColumns = @JoinColumn(name="user_role_id"),
        inverseJoinColumns = @JoinColumn(name="site_id")
    )
    public Set<Site> getSites() {
        return sites;
    }

    public void setSites(Set<Site> sites) {
        this.sites = sites;
    }

    public void addSite(Site site) {
        sites.add(site);
    }

    public void removeSite(Site site) {
        if (sites.contains(site)) {
            sites.remove(site);
        }
    }

    @ManyToMany
    @JoinTable( name="user_role_study_sites",
        joinColumns = @JoinColumn(name="user_role_id"),
        inverseJoinColumns = @JoinColumn(name="study_site_id")
    )
    public List<StudySite> getStudySites() {
        return studySites;
    }

    public void setStudySites(List<StudySite> studySites) {
        this.studySites = studySites;
    }

    public void addStudySite(StudySite studySite) {
        studySites.add(studySite);
    }

    public void clearStudySites() {
        studySites.clear();
    }

    public void removeStudySite(StudySite studySite) {
        studySites.remove(studySite);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRole userRole = (UserRole) o;

        if (user != null ? !user.equals(userRole.user) : userRole.user != null) return false;
        if (role != null ? !role.equals(userRole.role) : userRole.role != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = (user != null ? user.hashCode() : 0);
        result = 29 * result + (role != null ? role.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return new StringBuilder(getClass().getSimpleName()).
            append("[role=").append(getRole()).
            append("; sites=").append(getSites()).
            append(']').toString();
    }
}
