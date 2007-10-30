package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Named;
import edu.northwestern.bioinformatics.studycalendar.utils.NamedComparator;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public abstract class AbstractSiteCoordinatorDashboardCommand<R extends Named, C extends Named> extends AbstractGridCommand <R, C>{
    private List<Study> assignableStudies;
    private List<Site> assignableSites;
    private List<User> assignableUsers;

    protected AbstractSiteCoordinatorDashboardCommand(List<Study> assignableStudies, List<Site> assignableSites, List<User> assignableUsers) {
        this.assignableStudies = assignableStudies;
        this.assignableSites = assignableSites;
        this.assignableUsers = assignableUsers;
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
}
