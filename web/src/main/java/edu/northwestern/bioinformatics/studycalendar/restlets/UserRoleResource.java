package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlSerializer;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;


/**
 * @author Jalpa Patel
 */
public class UserRoleResource extends AbstractPscResource {
    private PscUserService pscUserService;
    private StudyCalendarXmlSerializer<SuiteRoleMembership> xmlSerializer;

    private SuiteRoleMembership suiteRoleMembership;

    @Override
    public void doInit() {
        super.doInit();
        setAllAuthorizedFor(Method.GET);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        suiteRoleMembership = loadRequestedObject();
        setExisting(suiteRoleMembership != null);
    }

    protected SuiteRoleMembership loadRequestedObject() throws ResourceException {
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
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType().equals(MediaType.TEXT_XML)) {
            return createXmlRepresentation(suiteRoleMembership);
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
