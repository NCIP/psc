package edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * @author Rhett Sutphin
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AccessControl {
    StudyCalendarProtectionGroup[] protectionGroups(); 
}
