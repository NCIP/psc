package edu.northwestern.bioinformatics.studycalendar.web.admin;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.web.PscCancellableFormController;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.AccessControl;

@AccessControl(roles = Role.SYSTEM_ADMINISTRATOR)
public class PurgeStudyController extends PscCancellableFormController {
    public PurgeStudyController() {
        setFormView("admin/purgeStudy");
        setCommandClass(PurgeStudyCommand.class);
    }
}
