package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;

import java.util.Date;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<Study> {
    private AmendedTemplateHelper amendedTemplateHelper;

    @Override
    public void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        setAllAuthorizedFor(Method.GET);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation representation = super.represent(variant);
        Date modifiedDate = getRequestedObject().getLastModifiedDate();
        representation.setModificationDate(modifiedDate);
        return representation;
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        try {
            return amendedTemplateHelper.getAmendedTemplate(request);
        } catch (AmendedTemplateHelper.NotFound notFound) {
            setClientErrorReason(notFound.getMessage());
            return null;
        }
    }

    ////// CONFIGURATION

    public void setAmendedTemplateHelper(AmendedTemplateHelper amendedTemplateHelper) {
        this.amendedTemplateHelper = amendedTemplateHelper;
    }
}
