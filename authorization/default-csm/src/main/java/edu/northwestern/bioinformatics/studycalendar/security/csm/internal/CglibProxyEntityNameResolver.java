package edu.northwestern.bioinformatics.studycalendar.security.csm.internal;

import org.hibernate.EntityNameResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Rhett Sutphin
 */
public class CglibProxyEntityNameResolver implements EntityNameResolver {
    private Pattern CGLIB_ENHANCED_CLASS_NAME =
        Pattern.compile("^\\$?(.*)\\$\\$EnhancerByCGLIB\\$\\$\\w+$");

    public String resolveEntityName(Object o) {
        Matcher m = CGLIB_ENHANCED_CLASS_NAME.matcher(o.getClass().getName());
        if (m.matches()) {
            return m.group(1);
        }
        return null;
    }
}
