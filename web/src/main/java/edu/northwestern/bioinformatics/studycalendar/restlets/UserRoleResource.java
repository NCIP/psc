package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.restlet.data.*;
import org.restlet.Context;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author Jalpa Patel
 */
public class UserRoleResource extends AbstractPscResource {
    private PscUserService pscUserService;
    private StudyCalendarXmlSerializer<SuiteRoleMembership> xmlSerializer;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
    }

    protected SuiteRoleMembership getRequestedObject() throws ResourceException {
        String userName = UriTemplateParameters.USERNAME.extractFrom(getRequest());
        String roleName = UriTemplateParameters.ROLENAME.extractFrom(getRequest());
        if (userName == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No user name in request");
        }

        if (roleName == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No role name in request");
        }
        PscUser user = pscUserService.loadUserByUsername(userName);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user " + userName);
        } else {
            SuiteRoleMembership suiteRoleMembership = findUserRoleMembership(user, roleName);
            if (getCurrentUser().getName().equals(userName)) {
                return suiteRoleMembership;
            } else if (getCurrentUser().getMembership(PscRole.USER_ADMINISTRATOR) != null) {
                return suiteRoleMembership;
            } else if (getCurrentUser().getMembership(PscRole.SYSTEM_ADMINISTRATOR) != null && user.getMembership(PscRole.USER_ADMINISTRATOR) != null) {
                return suiteRoleMembership;
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, getCurrentUser().getName() + " has insufficient privilege");
    }

    public SuiteRoleMembership findUserRoleMembership(PscUser user, String roleName) throws ResourceException {
        for (SuiteRoleMembership suiteRoleMembership : user.getMemberships().values() ) {
            if (suiteRoleMembership.getRole().getDisplayName().equals(roleName)) {
                return suiteRoleMembership;
            }
        }
        return null;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            SuiteRoleMembership userRole = getRequestedObject();
            if (userRole != null) {
                return createXmlRepresentation(userRole);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private Representation createXmlRepresentation(SuiteRoleMembership suiteRoleMembership) {
        return new StringRepresentation(
                getXmlSerializer().createDocumentString(suiteRoleMembership), MediaType.TEXT_XML);
    }

    public StudyCalendarXmlSerializer<SuiteRoleMembership> getXmlSerializer() {
        return xmlSerializer;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlSerializer<SuiteRoleMembership> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}
