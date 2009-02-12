package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;

import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.id.IdentityGenerator;

/**
 * @author Rhett Sutphin
 */
public class ImprovedPostgreSQLDialect extends PostgreSQLDialect {
    public Class getNativeIdentifierGeneratorClass() {
        return IdentityGenerator.class;
    }
}
