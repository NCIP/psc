package edu.northwestern.bioinformatics.studycalendar.web;

import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;

import java.util.List;

public interface AbstractSiteCoordinatorDashboardCommand {
   // TODO: abstract common methods into this

    public List<Study> getAssignableStudies();

    public List<Site> getAssignableSites();

    public List<User> getAssignableUsers();

    public void apply() throws Exception;
}
