package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Role;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 * @author Saruabh Agrawal
 */
public class AmendmentApprovalsResource extends StudySiteCollectionResource<AmendmentApproval> {
    private AmendmentService amendmentService;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAuthorizedFor(Method.GET, Role.SUBJECT_COORDINATOR);
        setAuthorizedFor(Method.POST, Role.SUBJECT_COORDINATOR);
    }


    @Override
    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
                xmlSerializer.createDocumentString(studySite.getAmendmentApprovals()), MediaType.TEXT_XML);
    }

    @Override
    protected String acceptValue(AmendmentApproval amendmentApproval) throws ResourceException {
        amendmentService.approve(getStudySite(), amendmentApproval);
        return String.format("studies/%s/sites/%s/approvals/%s",
                getStudySite().getStudy().getNaturalKey(), getStudySite().getSite().getNaturalKey(),
                amendmentApproval.getAmendment().getNaturalKey());
    }

    ////// CONFIGURATION


    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }
}
