/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.com/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.restlets.representations.UserListJsonRepresentation;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscUser;
import edu.northwestern.bioinformatics.studycalendar.service.PscUserService;
import edu.northwestern.bioinformatics.studycalendar.service.presenter.VisibleAuthorizationInformation;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRole;
import gov.nih.nci.cabig.ctms.suite.authorization.SuiteRoleMembership;
import gov.nih.nci.security.authorization.domainobjects.User;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

/**
 * @author Rhett Sutphin
 */
public class UsersResource extends AbstractPscResource {
    private PscUserService pscUserService;

    @Override
    public void doInit() {
        super.doInit();
        addAuthorizationsFor(Method.GET, PscRole.SYSTEM_ADMINISTRATOR, PscRole.USER_ADMINISTRATOR);
        getVariants().add(new Variant(MediaType.APPLICATION_JSON));
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        if (variant.getMediaType().includes(MediaType.APPLICATION_JSON)) {
            List<User> results = pscUserService.search(QueryParameters.Q.extractFrom(getRequest()));
            Integer limit = extractLimit();
            Integer offset = extractOffset(results.size(), limit);
            boolean brief = extractBrief();
            List<PscUser> toRender = buildRenderableUsers(results, brief, limit, offset);
            UserListJsonRepresentation rep =
                new UserListJsonRepresentation(toRender, brief, results.size(), offset, limit);

            if (!brief) {
                VisibleAuthorizationInformation visAuthInfo =
                    pscUserService.getVisibleAuthorizationInformationFor(getCurrentUser());
                rep.setVisibleSites(visAuthInfo.getSites());
                rep.setVisibleManagedStudyIdentifiers(
                    identifiersFor(visAuthInfo.getStudiesForTemplateManagement()));
                rep.setVisibleParticipatingStudyIdentifiers(
                    identifiersFor(visAuthInfo.getStudiesForSiteParticipation()));
            }

            return rep;
        } else {
            return super.get(variant);
        }
    }

    private List<String> identifiersFor(List<Study> studies) {
        List<String> idents = new ArrayList<String>(studies.size());
        for (Study study : studies) {
            idents.add(study.getAssignedIdentifier());
        }
        return idents;
    }

    private Integer extractLimit() throws ResourceException {
        String limitS = QueryParameters.LIMIT.extractFrom(getRequest());
        if (limitS == null) return null;
        try {
            Integer limit = new Integer(limitS);
            if (limit < 1) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
            }
            return limit;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Limit must be a positive integer.");
        }
    }

    private Integer extractOffset(int total, Integer limit) throws ResourceException {
        String offsetS = QueryParameters.OFFSET.extractFrom(getRequest());
        if (offsetS == null) return 0;
        if (limit == null) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset does not make sense without limit.");
        }
        try {
            Integer offset = new Integer(offsetS);
            if (offset < 0) {
                throw new ResourceException(
                    CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
            }
            if (offset >= total && offset > 0) {
                throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, String.format(
                    "Offset %d is too large.  There are %d result(s), so the max offset is %d.",
                    offset, total, Math.max(0, total - 1)));
            }
            return offset;
        } catch (NumberFormatException nfe) {
            throw new ResourceException(
                CLIENT_ERROR_BAD_REQUEST, "Offset must be a nonnegative integer.");
        }
    }

    private boolean extractBrief() throws ResourceException {
        String briefS = QueryParameters.BRIEF.extractFrom(getRequest());
        if (briefS == null) return true;
        // bizarrely, the JDK won't do this for you.  Anything that isn't "true" is false.
        if ("true".equalsIgnoreCase(briefS)) {
            return true;
        } else if ("false".equalsIgnoreCase(briefS)) {
            return false;
        } else {
            throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, 
                "Brief flag must be either \"true\" or \"false\" (or omitted).");
        }
    }

    private List<PscUser> buildRenderableUsers(
        List<User> results, boolean brief, Integer limit, Integer offset
    ) {
        List<User> toWrap;
        if (limit == null) {
            toWrap = results;
        } else {
            toWrap = results.subList(offset, Math.min(offset + limit, results.size()));
        }
        List<PscUser> toRender;
        if (brief) {
            toRender = new ArrayList<PscUser>(toWrap.size());
            for (User result : toWrap) {
                toRender.add(new PscUser(
                    result, Collections.<SuiteRole, SuiteRoleMembership>emptyMap()));
            }
        } else {
            toRender = pscUserService.getPscUsers(toWrap, false);
        }
        return toRender;
    }

    ////// CONFIGURATION

    @Required
    public void setPscUserService(PscUserService pscUserService) {
        this.pscUserService = pscUserService;
    }
}
