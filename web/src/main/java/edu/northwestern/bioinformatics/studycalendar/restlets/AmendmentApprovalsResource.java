package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarValidationException;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.AmendmentApprovalXmlSerializer;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

import static edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole.*;
import static edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization.createSiteParticipationAuthorizations;

/**
 * @author Rhett Sutphin
 * @author Saruabh Agrawal
 */
public class AmendmentApprovalsResource extends StudySiteCollectionResource<AmendmentApproval> {
    private AmendmentService amendmentService;

    @Override
    public void doInit() {
        super.doInit();

        addAuthorizationsFor(Method.GET, createSiteParticipationAuthorizations(getStudy()));
        addAuthorizationsFor(Method.POST, getSite(), STUDY_QA_MANAGER);

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
