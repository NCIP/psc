/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.test.integrated;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Rhett Sutphin
 */
public class MockConnectionSource extends ConnectionSource {
    private JdbcTemplate jdbcTemplate;

    public MockConnectionSource(JdbcTemplate jdbcTemplate) {
        super(null);
        this.jdbcTemplate = jdbcTemplate;
    }

    public synchronized JdbcTemplate currentJdbcTemplate() {
        return jdbcTemplate;
    }
}
