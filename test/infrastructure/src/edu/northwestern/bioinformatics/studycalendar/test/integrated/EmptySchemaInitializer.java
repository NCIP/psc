package edu.northwestern.bioinformatics.studycalendar.test.integrated;

/**
 * An empty implementation of {@link SchemaInitializer}.
 *
 * @author Rhett Sutphin
 */
public class EmptySchemaInitializer implements SchemaInitializer {
    public void oneTimeSetup(ConnectionSource connectionSource) { }

    public void beforeAll(ConnectionSource connectionSource)  { }
    public void beforeEach(ConnectionSource connectionSource) { }

    public void afterEach(ConnectionSource connectionSource) { }
    public void afterAll(ConnectionSource connectionSource)  { }
}
