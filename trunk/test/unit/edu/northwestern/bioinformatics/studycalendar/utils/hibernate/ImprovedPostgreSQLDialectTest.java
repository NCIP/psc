package edu.northwestern.bioinformatics.studycalendar.utils.hibernate;

import junit.framework.TestCase;
import org.hibernate.id.IdentityGenerator;

/**
 * @author Rhett Sutphin
 */
public class ImprovedPostgreSQLDialectTest extends TestCase {
    public void testNativeGenerator() throws Exception {
        assertSame(IdentityGenerator.class, new ImprovedPostgreSQLDialect().getNativeIdentifierGeneratorClass());
    }
}
