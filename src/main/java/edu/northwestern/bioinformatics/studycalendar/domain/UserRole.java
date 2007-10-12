package edu.northwestern.bioinformatics.studycalendar.domain;


import gov.nih.nci.cabig.ctms.domain.AbstractMutableDomainObject;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "user_roles")
@GenericGenerator(name="id-generator", strategy = "native",
    parameters = {
        @Parameter(name="sequence", value="seq_user_roles_id")
    }
)
public class UserRole  extends AbstractMutableDomainObject {
    User user;
    Role role;
    Set<Site> sites = new HashSet<Site>();

    @ManyToOne(fetch = FetchType.LAZY)
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
        sites.remove(site);
    }


    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserRole userRole = (UserRole) o;

        if (user != null ? !user.equals(userRole.user) : userRole.user != null) return false;
        if (role != null ? !role.equals(userRole.role) : userRole.role != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (user != null ? user.hashCode() : 0);
        result = 29 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}
