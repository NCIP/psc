package edu.northwestern.bioinformatics.studycalendar.web;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.apache.commons.lang.StringUtils.join;

public class LegacyUserProvisioningRecordDao {
    private DataSource dataSource;

    public LegacyUserProvisioningRecordDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SuppressWarnings("unchecked")
    public List<LegacyUserProvisioningRecord> getAll() {
        JdbcTemplate template = new JdbcTemplate(dataSource);

        String[] query = new String[] {
            "select name, first_name, last_name, csm_group_name,  s.name as site_name, '' as study_name, active_flag",
            "from Users u",
            "left join User_Roles ur on u.id = ur.user_id",
            "left join User_Role_Sites urs on ur.id = urs.user_role_id",
            "left join Sites s on urs.site_id = s.id",
            "order by name, csm_group_name, site_name, study_name"
        };

        return template.query(join(query, ' '), new PersonRowMapper());
    }

    public class PersonRowMapper implements RowMapper {
        public Object mapRow(ResultSet rs, int i) throws SQLException {
            LegacyUserProvisioningRecordResultSetExtractor extractor = new LegacyUserProvisioningRecordResultSetExtractor();
            return extractor.extractData(rs);

        }

    }

    public class LegacyUserProvisioningRecordResultSetExtractor implements ResultSetExtractor {
        public Object extractData(ResultSet rs) throws SQLException {
            return new LegacyUserProvisioningRecord(
                rs.getString("name"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("active_flag"),
                rs.getString("csm_group_name"),
                rs.getString("site_name"),
                rs.getString("study_name")
            );
        }

    }


}