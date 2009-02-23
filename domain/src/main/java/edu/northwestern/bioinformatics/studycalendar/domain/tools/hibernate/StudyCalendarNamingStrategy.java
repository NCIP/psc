package edu.northwestern.bioinformatics.studycalendar.domain.tools.hibernate;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * @author Rhett Sutphin
 */
public class StudyCalendarNamingStrategy extends ImprovedNamingStrategy {
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        return columnName(propertyName) + "_id";
    }

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
}
