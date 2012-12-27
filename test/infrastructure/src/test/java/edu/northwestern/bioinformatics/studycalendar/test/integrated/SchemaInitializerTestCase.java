/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import edu.northwestern.bioinformatics.studycalendar.core.StudyCalendarTestCase;
import org.jvyaml.YAML;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public abstract class SchemaInitializerTestCase extends StudyCalendarTestCase {
    protected JdbcTemplate jdbc;
    protected ConnectionSource connectionSource;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jdbc = registerNiceMockFor(JdbcTemplate.class);
        connectionSource = new MockConnectionSource(jdbc);
    }

    @SuppressWarnings({ "unchecked" })
    public static Map<String, Object> yamlMap(String yaml) {
        return (Map<String, Object>) YAML.load(yaml);
    }

    public static Resource literalYamlResource(String yaml) {
        return new ByteArrayResource(yaml.getBytes());
    }
}
