package edu.northwestern.bioinformatics.studycalendar.web.dashboard.sitecoordinator;

import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.AbstractGridCommand;
import edu.northwestern.bioinformatics.studycalendar.web.osgi.InstalledAuthenticationSystem;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;

import java.util.List;

/**
 * @author John Dzak
 */
public abstract class AbstractAssignSubjectCoordinatorCommand<R extends Named, C extends Named> extends AbstractGridCommand<R, C> {
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;
    private InstalledAuthenticationSystem installedAuthenticationSystem;

    protected AbstractAssignSubjectCoordinatorCommand(List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers, InstalledAuthenticationSystem installedAuthenticationSystem) {
        this.assignableStudies = assignableStudies;
        this.assignableSites = assignableSites;
        this.assignableUsers = assignableUsers;
        this.installedAuthenticationSystem = installedAuthenticationSystem;
    }

    public List<Study> getAssignableStudies() {
        return assignableStudies;
    }

    public List<Site> getAssignableSites() {
        return assignableSites;
    }

    public List<User> getAssignableUsers() {
        return assignableUsers;
    }

    public InstalledAuthenticationSystem getInstalledAuthenticationSystem() {
        return installedAuthenticationSystem;
    }

    protected void refreshUser(User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            PscUser authenticatedUser = (PscUser) authentication.getPrincipal();
            if (user.getName().equals(authenticatedUser.getUsername())) {
                installedAuthenticationSystem.reloadAuthorities();
            }
        }
    }
}
