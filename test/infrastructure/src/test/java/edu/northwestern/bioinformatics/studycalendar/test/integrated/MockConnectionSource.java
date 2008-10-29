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
