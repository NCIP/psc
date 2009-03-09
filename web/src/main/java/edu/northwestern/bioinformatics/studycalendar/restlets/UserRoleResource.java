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
    protected UserRole loadRequestedObject(Request request) throws ResourceException {
        username = UriTemplateParameters.USERNAME.extractFrom(request);
        rolename = UriTemplateParameters.ROLENAME.extractFrom(request);
        Authentication authenticate = PscGuard.getCurrentAuthenticationToken(request);
        if (username == null) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No username in request");
        }
        user = userService.getUserByName(username);
        if (user == null) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Unknown user " + username);
        } else if (authenticate !=null){
            if (authenticate.getName().equals(username)) {
                return findUserRole(user);
            } else {
                for (GrantedAuthority authority : authenticate.getAuthorities()) {
                    if (authority.equals(Role.SYSTEM_ADMINISTRATOR)) {
                        return findUserRole(user);
                    }
                }
            }
        }
        throw new ResourceException(Status.CLIENT_ERROR_FORBIDDEN, authenticate.getName() + " is not allowed");
    }

    public UserRole findUserRole(User user) throws ResourceException {
        for (UserRole userRole: user.getUserRoles() ) {
            if (userRole.getRole().getDisplayName().equals(rolename)) {
                return userRole;
            }
        }
        return null;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
      if (getRequestedObject() != null ) {
        return super.represent(variant);
      } else {
          return null;
      }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
