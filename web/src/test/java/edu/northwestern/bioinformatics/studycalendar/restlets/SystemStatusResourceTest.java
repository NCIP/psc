package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;

import static org.easymock.EasyMock.expect;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Rhett Sutphin
 */
public class SystemStatusResourceTest extends ResourceTestCase<SystemStatusResource> {
    private JdbcTemplate jdbcTemplate;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jdbcTemplate = registerMockFor(JdbcTemplate.class);
    }

    @Override
    protected SystemStatusResource createResource() {
        SystemStatusResource r = new SystemStatusResource();
        r.setJdbcTemplate(jdbcTemplate);
        return r;
    }

    public void testIsGetOnly() throws Exception {
        assertAllowedMethods("GET");
    }

    public void testIsSuccessWhenDataSourceIsFine() throws Exception {
        expect(jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies")).andReturn(542);

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testReturnsDetailsWhenDataSourceIsFine() throws Exception {
        expect(jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies")).andReturn(542);

        JSONObject datasourceClause =
            doGetAndReturnJSON().getJSONObject("system-status").getJSONObject("datasource");
        assertThat(datasourceClause.optBoolean("ok"), is(true));
        assertThat(datasourceClause.optString("message"), is("Domain query successful"));
    }

    public void testIsAFailureWhenDataSourceIsNotFine() throws Exception {
        expect(jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies")).
            andThrow(new DataAccessResourceFailureException("Some other time perhaps"));

        doGet();
        assertResponseStatus(Status.SERVER_ERROR_INTERNAL);
    }

    public void testReturnsDetailsWhenDataSourceIsNotFine() throws Exception {
        expect(jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies")).
            andThrow(new DataAccessResourceFailureException("Some other time perhaps"));

        JSONObject datasourceClause =
            doGetAndReturnJSON().getJSONObject("system-status").getJSONObject("datasource");
        assertThat(datasourceClause.optBoolean("ok"), is(false));
        assertThat(datasourceClause.optString("message"), is("Some other time perhaps"));
    }

    private JSONObject doGetAndReturnJSON() throws IOException, JSONException {
        doGet();
        return new JSONObject(response.getEntity().getText());
    }
}
