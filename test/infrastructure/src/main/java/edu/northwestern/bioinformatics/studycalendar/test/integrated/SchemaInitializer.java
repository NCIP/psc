/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

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
