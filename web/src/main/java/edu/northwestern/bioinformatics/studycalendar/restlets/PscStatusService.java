package edu.northwestern.bioinformatics.studycalendar.restlets;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ResourceException;
import org.restlet.service.StatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rhett Sutphin
 */
public class PscStatusService extends StatusService {
    public static final String CLIENT_ERROR_REASON_KEY =
        PscStatusService.class.getName() + ".clientErrorReason";

    // not final so it can be overridden for testing
    private Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public Status getStatus(Throwable throwable, Request request, Response response) {
        if (throwable instanceof ResourceException) {
            ResourceException re = (ResourceException) throwable;
            if (re.getStatus().isClientError()) {
                log.info(re.getStatus().toString());
                if (re.getCause() != null) {
                    log.debug("Previous " + re.getStatus().getCode() + " was caused by this exception",
                        re.getCause());
                }
            } else if (re.getStatus().isServerError()) {
                if (re.getCause() == null) {
                    log.error(re.getStatus().toString());
                } else {
                    log.error(re.getStatus().toString(), re.getCause());
                }
            } else {
                // Some other status -- e.g., a restlet connector problem
                // FFR: restlet treats these as "INFO"
                if (re.getCause() == null) {
                    log.warn(re.getStatus().toString());
                } else {
                    log.warn(re.getStatus().toString(), re.getCause());
                }
            }

            return re.getStatus();
        } else {
            log.error("Uncaught exception in resource handling", throwable);
            return Status.SERVER_ERROR_INTERNAL;
        }
    }

    @Override
    public Representation getRepresentation(Status status, Request request, Response response) {
        StringBuilder message = new StringBuilder().
            append(status.getCode()).append(' ').append(status.getName());
        if (status.getDescription() != null) {
            message.append("\n\n").append(status.getDescription());
        }
        String clientErrorReason = (String) request.getAttributes().get(CLIENT_ERROR_REASON_KEY);
        if (clientErrorReason != null) {
            message.append("\n\n").append(clientErrorReason);
        }
        message.append('\n');

        return new StringRepresentation(message);
    }

    ////// CONFIGURATION

    /**
     * Set the target logger.  Intended for testing only.
     */
    protected void setLogger(Logger log) {
        this.log = log;
    }
}
