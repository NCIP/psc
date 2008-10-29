package edu.northwestern.bioinformatics.studycalendar.test.integrated;

/**
 * @author Rhett Sutphin
 */
public interface SchemaInitializer {
    void oneTimeSetup(ConnectionSource connectionSource);

    void beforeAll(ConnectionSource connectionSource);
    void beforeEach(ConnectionSource connectionSource);

    void afterEach(ConnectionSource connectionSource);
    void afterAll(ConnectionSource connectionSource);
}
