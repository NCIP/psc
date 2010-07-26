package edu.northwestern.bioinformatics.studycalendar.web.template;

import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedCommand;

import java.util.Map;

/**
 * @author nshurupova
 * @author Rhett Sutphin
 */
public interface EditCommand extends PscAuthorizedCommand {
    boolean apply();

    Map<String, Object> getModel();

    String getRelativeViewName();
}
