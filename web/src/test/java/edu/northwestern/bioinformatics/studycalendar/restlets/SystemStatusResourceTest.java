package edu.northwestern.bioinformatics.studycalendar.restlets;

import gov.nih.nci.cabig.ctms.suite.authorization.CsmHelper;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteAuthorizationAccessException;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.security.authorization.domainobjects.Group;
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
    private CsmHelper csmHelper;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        jdbcTemplate = registerMockFor(JdbcTemplate.class);
        csmHelper = registerMockFor(CsmHelper.class);

        expect(jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies")).andStubReturn(0);
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).andStubReturn(new Group());
    }

    @Override
    protected SystemStatusResource createResource() {
        SystemStatusResource r = new SystemStatusResource();
        r.setJdbcTemplate(jdbcTemplate);
        r.setCsmHelper(csmHelper);
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

    public void testIsSuccessWhenCsmIsFine() throws Exception {
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).andReturn(new Group());

        doGet();
        assertResponseStatus(Status.SUCCESS_OK);
    }

    public void testReturnsDetailsWhenCsmIsFine() throws Exception {
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).andReturn(new Group());

        JSONObject authorizationClause =
            doGetAndReturnJSON().getJSONObject("system-status").getJSONObject("csm");
        assertThat(authorizationClause.optBoolean("ok"), is(true));
        assertThat(authorizationClause.optString("message"), is("CSM available"));
    }

    public void testIsAFailureWhenCsmIsNotFine() throws Exception {
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).
            andThrow(new SuiteAuthorizationAccessException("No such-a one"));

        doGet();
        assertResponseStatus(Status.SERVER_ERROR_INTERNAL);
    }

    public void testReturnsDetailsWhenCsmIsNotFine() throws Exception {
        expect(csmHelper.getRoleCsmGroup(SuiteRole.DATA_READER)).
            andThrow(new SuiteAuthorizationAccessException("No such-a one"));

        JSONObject authorizationClause =
            doGetAndReturnJSON().getJSONObject("system-status").getJSONObject("csm");
        assertThat(authorizationClause.optBoolean("ok"), is(false));
        assertThat(authorizationClause.optString("message"), is("CSM data not available"));
    }

    private JSONObject doGetAndReturnJSON() throws IOException, JSONException {
        doGet();
        return new JSONObject(response.getEntity().getText());
    }
}
