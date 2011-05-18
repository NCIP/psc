package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.tools.MapBuilder;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Rhett Sutphin
 */
public class SystemStatusResource extends ServerResource {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private JdbcTemplate jdbcTemplate;

    @Override
    protected void doInit() throws ResourceException {
        super.doInit();

        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
        getAllowedMethods().add(Method.GET);
    }

    @Override
    protected Representation get(Variant variant) throws ResourceException {
        Collection<StatusCheck> checks = Arrays.<StatusCheck>asList(
            new DataSourceCheck()
        );
        boolean okay = true;
        Map<String, JSONObject> statusDoc = new LinkedHashMap<String, JSONObject>();
        for (StatusCheck check : checks) {
            check.doCheck();
            okay &= check.isOkay();
            statusDoc.put(check.name(), check.getClause());
        }
        if (!okay) {
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
        return new JsonRepresentation(
            new JSONObject(Collections.singletonMap("system-status", statusDoc)));
    }

    ////// CONFIGURATION

    @Required
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    ////// CHECK IMPLEMENTATIONS

    private abstract class StatusCheck {
        private boolean okay = true;
        private String message = null;

        /**
         * Template method for checkers
         * @return a success message if the check is successful.
         * @throws Exception if something is wrong. The exception message will be the message for the client.
         */
        protected abstract String check() throws Exception;
        protected abstract String name();

        public final void doCheck() {
            try {
                message = check();
                log.debug("System status check %s succeeded", getClass().getSimpleName());
            } catch (Exception e) {
                okay = false;
                message = e.getMessage();
                log.error(
                    String.format("System status check %s failed: %s",
                        getClass().getSimpleName(), e.getMessage()),
                    e);
            }
        }

        public JSONObject getClause() {
            return new JSONObject(
                new MapBuilder<String, Object>().put("ok", okay).put("message", message).toMap());
        }

        public boolean isOkay() {
            return okay;
        }
    }

    private class DataSourceCheck extends StatusCheck {
        @Override
        protected String check() throws Exception {
            jdbcTemplate.queryForInt("SELECT COUNT(id) FROM studies");
            return "Domain query successful";
        }

        @Override
        protected String name() {
            return "datasource";
        }
    }
}
