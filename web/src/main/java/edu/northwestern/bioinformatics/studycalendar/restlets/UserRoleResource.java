package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.UserRole;
import edu.northwestern.bioinformatics.studycalendar.domain.User;
import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.service.UserService;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.Context;
import org.restlet.resource.Representation;
import org.restlet.resource.Variant;
import org.restlet.resource.ResourceException;
import org.acegisecurity.Authentication;
import org.acegisecurity.GrantedAuthority;


/**
 * @author Jalpa Patel
 */
public class UserRoleResource extends AbstractDomainObjectResource<UserRole> {
    private UserService userService;
    private User user;
    private String username;
    private String rolename;
    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    protected UserRole loadRequestedObject(Request request) {
        username = UriTemplateParameters.USERNAME.extractFrom(request);
        rolename = UriTemplateParameters.ROLENAME.extractFrom(request);
        if (username == null) {
            setClientErrorReason("No username in request");
            return null;
        }
        user = userService.getUserByName(username);
        if (user == null) {
            setClientErrorReason("Unknown user " + username);
        } else {
            for (UserRole userRole: user.getUserRoles() ) {
                if (userRole.getRole().getDisplayName().equals(rolename)) {
                    return userRole;
                }
            }
        }
        return null;
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

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
