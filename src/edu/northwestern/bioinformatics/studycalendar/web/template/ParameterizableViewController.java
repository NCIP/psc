package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.AccessControl;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.StudyCalendarProtectionGroup;

/**
 * Need a subclass here solely to apply the access control annotation.
 */
@AccessControl(protectionGroups = StudyCalendarProtectionGroup.STUDY_COORDINATOR)
public class ParameterizableViewController extends org.springframework.web.servlet.mvc.ParameterizableViewController { }
