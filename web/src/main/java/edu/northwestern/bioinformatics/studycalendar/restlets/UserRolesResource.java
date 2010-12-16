package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Jalpa Patel
 */
public class UserRolesResource extends AbstractPscResource {
    private PscUserService pscUserService;
    private StudyCalendarXmlCollectionSerializer<SuiteRoleMembership> xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<SuiteRoleMembership> getAllObjects() throws ResourceException {
        String userName = UriTemplateParameters.USERNAME.extractFrom(getRequest());
        if (userName == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No user name in request");
        }

        PscUser user = pscUserService.loadUserByUsername(userName);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user " + userName);
        } else {
            Collection<SuiteRoleMembership> userRoles =  user.getMemberships().values();
            if (getCurrentUser().getName().equals(userName)) {
                return userRoles;
            } else if(getCurrentUser().getMembership(PscRole.USER_ADMINISTRATOR) != null) {
                return userRoles;
            } else if (getCurrentUser().getMembership(PscRole.SYSTEM_ADMINISTRATOR) != null && user.getMembership(PscRole.USER_ADMINISTRATOR) != null) {
                Collection<SuiteRoleMembership> visibleMemberships = new ArrayList<SuiteRoleMembership>();
                visibleMemberships.add(user.getMembership(PscRole.USER_ADMINISTRATOR));
                if (user.getMembership(PscRole.SYSTEM_ADMINISTRATOR) != null) {
                    visibleMemberships.add(user.getMembership(PscRole.SYSTEM_ADMINISTRATOR));
                }
                return visibleMemberships;
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, getCurrentUser().getName() + " has insufficient privilege");
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(getAllObjects());
        } else {
            return null;
        }
    }

    private Representation createXmlRepresentation(Collection<SuiteRoleMembership> instances) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(instances), MediaType.TEXT_XML);
    }

    public StudyCalendarXmlCollectionSerializer<SuiteRoleMembership> getXmlSerializer() {
        return xmlSerializer;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<SuiteRoleMembership> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}
