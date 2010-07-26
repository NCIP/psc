package edu.northwestern.bioinformatics.studycalendar.web.tools;

import edu.northwestern.bioinformatics.studycalendar.core.accesscontrol.ApplicationSecurityManager;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.LegacyModeSwitch;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.PscAuthorizedHandler;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Rhett Sutphin
 */
public class PscAuthorizationInterceptor extends HandlerInterceptorAdapter {
    private final Logger log = LoggerFactory.getLogger(getClass());

    private LegacyModeSwitch legacyModeSwitch;
    private ApplicationSecurityManager applicationSecurityManager;

    @Override
    @SuppressWarnings({ "unchecked" })
    public boolean preHandle(
        HttpServletRequest request, HttpServletResponse response, Object handler
    ) throws Exception {
        if (legacyModeSwitch.isOn()) {
            log.debug("Skipping interceptor authorization in legacy mode");
            return true;
        } else {
            if (!(handler instanceof PscAuthorizedHandler)) {
                log.warn("Encountered secured handler with no authorization information ({}).  It will never execute.", handler);
                return forbidden(response);
            }

            log.debug("Performing interceptor authorization for {}", handler);

            PscUser user = applicationSecurityManager.getUser();
            if (user == null) {
                log.debug("Blocking execution of {} because no one is logged in", handler);
                return forbidden(response);
            }

            Collection<ResourceAuthorization> authorizations;
            try {
                authorizations = ((PscAuthorizedHandler) handler).authorizations(request.getMethod(), request.getParameterMap());
            } catch (Exception e) {
                log.error("Extracting authorizations from " + handler + " failed.  Locking down.", e);
                return forbidden(response);
            }

            for (ResourceAuthorization authorization : authorizations) {
                if (authorization.permits(user)) return true;
            }

            log.info("Forbidding execution of {} for {} because the user does not have sufficient privileges.",
                handler.getClass().getName(), request.getRequestURI());
            log.info("- One of these is required: {}", authorizations);
            log.info("- User has these roles {}", Arrays.asList(user.getAuthorities()));
            return forbidden(response);
        }
    }

    private boolean forbidden(HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
        return false;
    }

    ////// CONFIGURATION

    @Required
    public void setLegacyModeSwitch(LegacyModeSwitch legacyModeSwitch) {
        this.legacyModeSwitch = legacyModeSwitch;
    }

    @Required
    public void setApplicationSecurityManager(ApplicationSecurityManager applicationSecurityManager) {
        this.applicationSecurityManager = applicationSecurityManager;
    }
}
