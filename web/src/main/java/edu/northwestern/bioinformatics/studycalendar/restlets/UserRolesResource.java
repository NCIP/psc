package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.xml.StudyCalendarXmlCollectionSerializer;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.springframework.beans.factory.annotation.Required;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.data.Status;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.restlet.Context;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;

import java.util.Collection;

/**
 * @author Jalpa Patel
 */
public class UserRolesResource extends AbstractCollectionResource<UserRole> {
    private UserService userService;
    private User user;
    private StudyCalendarXmlCollectionSerializer<UserRole> xmlSerializer;
    private String username;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    @SuppressWarnings({ "ThrowInsideCatchBlockWhichIgnoresCaughtException" })
    public Collection<UserRole> getAllObjects() throws ResourceException {
        username = UriTemplateParameters.USERNAME.extractFrom(getRequest());
        if (username == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No username in request");
        }

        user = userService.getUserByName(username);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user " + username);
        } else {
            return user.getUserRoles();
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = super.represent(variant);
        Authentication authenticate = PscGuard.getCurrentAuthenticationToken(getRequest());
        if (authenticate.getName().equals(username)) {
           return representation;
        } else {
              for (GrantedAuthority authority : authenticate.getAuthorities()) {
                   if (authority.equals(Role.SYSTEM_ADMINISTRATOR)) {
                      return representation;
                   }
              }
        }
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, authenticate.getName() + " is not allowed");
    }

    @Override
    public StudyCalendarXmlCollectionSerializer<UserRole> getXmlSerializer() {
        return xmlSerializer;
    }

    @Required
    public void setXmlSerializer(StudyCalendarXmlCollectionSerializer<UserRole> xmlSerializer) {
        this.xmlSerializer = xmlSerializer;
    }
    
    @Required
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
