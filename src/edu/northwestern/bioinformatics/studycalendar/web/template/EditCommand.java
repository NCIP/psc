package edu.northwestern.bioinformatics.studycalendar.web.template;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: nshurupova
 * Date: Sep 14, 2007
 * Time: 3:52:55 PM
 * To change this template use File | Settings | File Templates.
 */
public interface EditCommand<T> {
    T apply();

    Map<String, Object> getModel();

    String getRelativeViewName();
}
