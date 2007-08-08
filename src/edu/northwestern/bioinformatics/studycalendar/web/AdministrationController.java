package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.web.servlet.mvc.ParameterizableViewController;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_ADMINISTRATOR)
public class AdministrationController extends ParameterizableViewController{
    public AdministrationController() {
        setViewName("administration");
    }
}
