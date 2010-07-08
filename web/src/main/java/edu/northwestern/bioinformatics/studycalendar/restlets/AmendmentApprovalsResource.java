package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.AmendmentApprovalXmlSerializer;
import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import org.restlet.Context;
import org.restlet.data.*;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;

/**
 * @author Rhett Sutphin
 * @author Saruabh Agrawal
 */
public class AmendmentApprovalsResource extends StudySiteCollectionResource<AmendmentApproval> {
    private AmendmentService amendmentService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);

        //TODO -- need to delete setAllAuthorizedFor and setAuthorizedFor when deleting the old user roles
        setAllAuthorizedFor(Method.GET);
        setAuthorizedFor(Method.POST, Role.SITE_COORDINATOR);

        addAuthorizationsFor(Method.GET, getSite(), getStudy(),
                STUDY_TEAM_ADMINISTRATOR,
                STUDY_SUBJECT_CALENDAR_MANAGER,
                DATA_READER
        );
        addAuthorizationsFor(Method.POST, getSite(), getStudy(), STUDY_SUBJECT_CALENDAR_MANAGER);

        ((AmendmentApprovalXmlSerializer) xmlSerializer).setStudy(getStudy());
    }

    @Override
    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
                xmlSerializer.createDocumentString(studySite.getAmendmentApprovals()), MediaType.TEXT_XML);
    }

    @Override
    protected String acceptValue(AmendmentApproval amendmentApproval) throws ResourceException {
        try {
            amendmentService.resolveAmentmentApproval(amendmentApproval, getStudy());
            amendmentService.approve(getStudySite(), amendmentApproval);
            return String.format("studies/%s/sites/%s/approvals/%s",
                getStudySite().getStudy().getNaturalKey(), getStudySite().getSite().getNaturalKey(),
                amendmentApproval.getAmendment().getNaturalKey());
        } catch (StudyCalendarValidationException scve) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, scve.getMessage());
        }
    }

    ////// CONFIGURATION

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }
}
