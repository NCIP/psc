package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.core.osgi.OsgiLayerTools;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.OsgiRepresentationHelper;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.StreamingJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.tools.MapBasedDictionary;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ManagedService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Map;
import java.util.regex.Pattern;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.SYSTEM_ADMINISTRATOR;

/**
 * @author Rhett Sutphin
 */
public class OsgiServicePropertiesResource extends OsgiSingleBundleResource {
    private static final Pattern IGNORED_PROPERTIES =
        Pattern.compile("^(service\\.|" + Constants.OBJECTCLASS + ")");

    private ServiceReference serviceReference;

    private OsgiLayerTools osgiLayerTools;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        addAuthorizationsFor(Method.PUT, SYSTEM_ADMINISTRATOR);
    }

    @Override public boolean allowPut() { return true; }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            final ServiceReference ref = getServiceReference();
            if (log.isDebugEnabled()) {
                log.debug("Found ref {} with properties:", ref);
                for (String key : ref.getPropertyKeys()) {
                    log.debug("- {} = {}", key, ref.getProperty(key));
                }
            }
            return new StreamingJsonRepresentation() {
                @Override
                public void generate(JsonGenerator generator) throws IOException, JsonGenerationException {
                    OsgiRepresentationHelper.writeServiceProperties(generator, ref);
                }
            };
        } else {
            return null;
        }
    }

    @Override
    @SuppressWarnings({ "RawUseOfParameterizedType" })
    public void storeRepresentation(Representation representation) throws ResourceException {
        if (representation.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            verifyPutParameters();
            try {
                Dictionary newProperties = buildNewConfiguration(representation.getText());
                osgiLayerTools.updateConfiguration(newProperties, getBundle(),
                    (String) getServiceReference().getProperty(Constants.SERVICE_PID));
            } catch (IOException e) {
                throw new ResourceException(e);
            }
        } else {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Only JSON is supported for PUT");
        }
    }

    @SuppressWarnings({ "RawUseOfParameterizedType" })
    private Dictionary buildNewConfiguration(String text) throws ResourceException {
        Map<String, Object> flat = OsgiRepresentationHelper.flattenJsonConfiguration(text);
        Dictionary<String, Object> newProperties = new MapBasedDictionary<String, Object>();
        for (Map.Entry<String, Object> entry : flat.entrySet()) {
            if (!IGNORED_PROPERTIES.matcher(entry.getKey()).find()) {
                newProperties.put(entry.getKey(), entry.getValue());
            }
        }
        return newProperties;
    }

    private void verifyPutParameters() throws ResourceException {
        if (!isManagedService()) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                "The selected service is not manageable.  It does not export ManagedService");
        }
        if (getServiceReference().getProperty(Constants.SERVICE_PID) == null) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                "No service.pid for managed service");
        }
    }

    private boolean isManagedService() throws ResourceException {
        return Arrays.asList((String[]) getServiceReference().getProperty(Constants.OBJECTCLASS)).
            contains(ManagedService.class.getName());
    }

    private synchronized ServiceReference getServiceReference() throws ResourceException {
        if (serviceReference == null) {
            String ident = UriTemplateParameters.SERVICE_IDENTIFIER.extractFrom(getRequest());
            Long id = null;
            try {
                id = new Long(ident);
            } catch (NumberFormatException nfe) {
                // PID is the only possibility
            }
            ServiceReference[] refs = getBundle().getRegisteredServices();
            if (refs == null) throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "No services for bundle");
            for (ServiceReference reference : refs) {
                if (ident.equals(reference.getProperty(Constants.SERVICE_PID))) {
                    serviceReference = reference;
                    break;
                } else if (id != null && id.equals(reference.getProperty(Constants.SERVICE_ID))) {
                    serviceReference = reference;
                    break;
                }
            }
            if (serviceReference == null) {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, ident + " does not match any service's PID or ID");
            }
        }
        return serviceReference;
    }

    ////// CONFIGURATION

    @Required
    public void setOsgiLayerTools(OsgiLayerTools osgiLayerTools) {
        this.osgiLayerTools = osgiLayerTools;
    }
}
