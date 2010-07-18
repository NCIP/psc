package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarNamingStrategy extends ImprovedNamingStrategy {
    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        return super.foreignKeyColumnName(propertyName,
            propertyEntityName, singularize(propertyTableName), referencedColumnName) + "_id";
    }

    @Override
    public String classToTableName(String className) {
        return pluralize(super.classToTableName(className));
    }

    private String pluralize(String name) {
        StringBuilder p = new StringBuilder(name);
        if (name.endsWith("y")) {
            p.deleteCharAt(p.length() - 1);
            p.append("ies");
        } else {
            p.append('s');
        }
        return p.toString();
    }

    private String singularize(String name) {
        StringBuilder p = new StringBuilder(name);
        if (name.endsWith("ies")) {
            p.delete(p.length() - 3, p.length());
            p.append('y');
        } else {
            p.deleteCharAt(p.length() - 1);
        }
        return p.toString();
    }
}
