/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.factory.annotation.Required;

import java.util.Date;

/**
 * @author John Dzak
 * @author Rhett Sutphin
 */
public class AmendedTemplateResource extends AbstractDomainObjectResource<Study> {
    private AmendedTemplateHelper helper;

    @Override
    public void doInit() {
        helper.setRequest(getRequest());
        super.doInit();
        addAuthorizationsFor(Method.GET, helper.getReadAuthorizations());
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation representation = super.get(variant);
        Date modifiedDate = getRequestedObject().getLastModifiedDate();
        representation.setModificationDate(modifiedDate);
        return representation;
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        try {
            return helper.getAmendedTemplate();
        } catch (AmendedTemplateHelper.NotFound notFound) {
            setClientErrorReason(notFound.getMessage());
            return null;
        }
    }

    ////// CONFIGURATION

    @Required
    public void setAmendedTemplateHelper(AmendedTemplateHelper amendedTemplateHelper) {
        this.helper = amendedTemplateHelper;
    }
}
