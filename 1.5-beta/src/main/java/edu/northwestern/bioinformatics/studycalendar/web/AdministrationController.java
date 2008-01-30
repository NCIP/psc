package edu.northwestern.bioinformatics.studycalendar.web;

import static edu.northwestern.bioinformatics.studycalendar.domain.Role.SYSTEM_ADMINISTRATOR;
import org.springframework.web.servlet.mvc.ParameterizableViewController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.CrumbSource;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.Crumb;
import edu.northwestern.bioinformatics.studycalendar.utils.breadcrumbs.DefaultCrumb;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

@AccessControl(roles = {Role.STUDY_ADMIN, Role.SYSTEM_ADMINISTRATOR})
public class AdministrationController extends ParameterizableViewController implements CrumbSource {
    private DefaultCrumb crumb;

    public AdministrationController() {
        setViewName("administration");
        crumb = new DefaultCrumb("Admin");
    }

    public Crumb getCrumb() {
        return crumb;
    }
}
