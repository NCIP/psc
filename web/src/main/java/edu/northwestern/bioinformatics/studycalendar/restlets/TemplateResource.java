/*L
 * Copyright Northwestern University.
 *
 * Distributed under the OSI-approved BSD 3-Clause License.
 * See http://ncip.github.io/psc/LICENSE.txt for details.
 */

package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.importer.TemplateImportService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.xml.validators.XMLValidator;
import org.apache.commons.io.IOUtils;
import org.restlet.Request;
import org.restlet.data.Disposition;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Resource representing a study and its planned calendar, including all amendments.
 *
 * @author Rhett Sutphin
 */
public class TemplateResource extends AbstractDomainObjectResource<Study> {
    private boolean useDownloadMode;
    private StudyDao studyDao;
    private StudyService studyService;
    private TemplateImportService templateImportService;

    @Override
    public Representation get(Variant variant) throws ResourceException {
        Representation representation = super.get(variant);
        if (useDownloadMode) {
            representation.setDisposition(new Disposition(Disposition.TYPE_ATTACHMENT));
            representation.getDisposition().setFilename(getRequestedObject().getAssignedIdentifier() + ".xml");
        }
        representation.setModificationDate(getRequestedObject().getLastModifiedDate());
        return representation;
    }

    @Override
    public void doInit() {
        super.doInit();
        Study study = getRequestedObjectDuringInit();
        addAuthorizationsFor(Method.GET, ResourceAuthorization.createAllStudyAuthorizations(study));
        addAuthorizationsFor(Method.PUT,
            ResourceAuthorization.createTemplateManagementAuthorizations(study, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));

        useDownloadMode = getRequest().getResourceRef().getQueryAsForm().getNames().contains("download");
    }

    @Override
    protected Study loadRequestedObject(Request request) {
        String studyIdent = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        Study base = studyDao.getByAssignedIdentifier(studyIdent);
        if (base == null) {
            base = studyDao.getByGridId(studyIdent);
        }
        if (base != null && request.getMethod().equals(Method.GET)) {
            return studyService.getCompleteTemplateHistory(base);
        }
        return base;
    }

    @Override
    public Representation put(Representation entity, Variant variant) throws ResourceException {
        Study out;
        try {
            String in = entity.getText();
            String error = getTemplateSchemaValidator().validate(IOUtils.toInputStream(in));
            if (isNotBlank(error)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, error);
            }
            Study imported = templateImportService.readAndSaveTemplate(getRequestedObject(), IOUtils.toInputStream(in));
            out = studyService.getCompleteTemplateHistory(imported);
        } catch (IOException e) {
            log.warn("PUT failed with IOException", e);
            throw new ResourceException(e);
        }
        getResponse().setEntity(createXmlRepresentation(out));
        if (getRequestedObject() == null) {
            getResponse().setStatus(Status.SUCCESS_CREATED);
        } else {
            getResponse().setStatus(Status.SUCCESS_OK);
        }

        return null;
    }

    public XMLValidator.BasicXMLValidator getTemplateSchemaValidator() {
        return XMLValidator.BASIC_TEMPLATE_VALIDATOR_INSTANCE;
    }

    ////// CONFIGURATION

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setTemplateImportService(TemplateImportService templateImportService) {
        this.templateImportService = templateImportService;
    }
}
