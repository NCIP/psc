package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.delta.AmendmentApproval;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.data.MediaType;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Rhett Sutphin
 */
public class AmendmentApprovalsResource extends StudySiteCollectionResource<AmendmentApproval> {
    private AmendmentService amendmentService;

    protected Representation createXmlRepresentation(StudySite studySite) throws ResourceException {
        return new StringRepresentation(
            studyCalendarXmlFactory.createDocumentString(studySite.getAmendmentApprovals(), null), MediaType.TEXT_XML);
    }

    protected String acceptValue(AmendmentApproval value) throws ResourceException {
        amendmentService.approve(getStudySite(), value);
        return String.format("studies/%s/sites/%s/approvals/%s", 
            getStudySite().getStudy().getNaturalKey(), getStudySite().getSite().getNaturalKey(),
            value.getAmendment().getNaturalKey());
    }

    ////// CONFIGURATION

    @Required
    public void setAmendmentService(AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }
}
