package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;

/**
 * Need a subclass here solely to apply the access control annotation.
 */
@AccessControl(roles = Role.STUDY_COORDINATOR)
public class ParameterizableViewController extends org.springframework.web.servlet.mvc.ParameterizableViewController { }
