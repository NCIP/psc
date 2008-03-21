package edu.northwestern.bioinformatics.studycalendar.web.template;

import java.util.Map;

/**
 * @author nshurupova
 */
public interface EditCommand {
    boolean apply();

    Map<String, Object> getModel();

    String getRelativeViewName();
}
