package edu.northwestern.bioinformatics.studycalendar.restlets;

import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.dao.delta.AmendmentDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.delta.Amendment;
import edu.northwestern.bioinformatics.studycalendar.security.authorization.PscRole;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateDevelopmentService;
import edu.northwestern.bioinformatics.studycalendar.web.accesscontrol.ResourceAuthorization;
import edu.northwestern.bioinformatics.studycalendar.xml.writers.AmendmentXmlSerializer;
import org.restlet.data.Method;
import org.restlet.Request;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.representation.Variant;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Required;

import java.io.IOException;
import java.util.Date;

/**
 * @author Saurabh Agrawal
 */
public class AmendmentResource extends AbstractRemovableStorableDomainObjectResource<Amendment> implements BeanFactoryAware {
    private StudyDao studyDao;
    private AmendmentDao amendmentDao;
    private Study study;
    private StudyService studyService;

    private BeanFactory beanFactory;
    private TemplateDevelopmentService templateDevelopmentService;

    @Override
    public void doInit() {
        super.doInit();
        getRequestedObjectDuringInit(); // for side effects
        addAuthorizationsFor(Method.GET, ResourceAuthorization.createAllStudyAuthorizations(study));
        addAuthorizationsFor(Method.PUT,
            ResourceAuthorization.createTemplateManagementAuthorizations(study, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
        addAuthorizationsFor(Method.DELETE,
            ResourceAuthorization.createTemplateManagementAuthorizations(study, PscRole.STUDY_CALENDAR_TEMPLATE_BUILDER));
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {

        Representation representation = super.get(variant);

        AmendmentXmlSerializer studyXmlSerializer = getXmlSerializer();
        try {
            // TODO: this is crazy -- why not just get the last modified date directly from the amendment
            // also, why is it being retrieved from the study?
            Date modifiedDate = studyXmlSerializer.readLastModifiedDate(representation.getStream());
            representation.setModificationDate(modifiedDate);
        } catch (IOException e) {
            log.warn("Study  does not have any modification date. Representation : " + representation);

        }

        return representation;
    }

    @Override
    protected Amendment loadRequestedObject(Request request) {
        String studyIdentifier = UriTemplateParameters.STUDY_IDENTIFIER.extractFrom(request);
        String amendmentIdentifier = UriTemplateParameters.AMENDMENT_IDENTIFIER.extractFrom(request);

        study = studyDao.getByAssignedIdentifier(studyIdentifier);
        if (study == null) {
            log.debug("No study matching {}", studyIdentifier);
            return null;
        }

        Amendment amendment;
        if (AmendedTemplateHelper.CURRENT.equals(amendmentIdentifier)) {
            amendment = study.getAmendment();
        } else {
            amendment = amendmentDao.getByNaturalKey(amendmentIdentifier, study);
        }
        if (amendment == null) {
            log.debug("No released  or development amendment matching {}", amendmentIdentifier);
            return null;
        }

        return amendment;
    }

    @Override
    public void remove(final Amendment amendment) {
        log.debug("Deleting amendment {} for study {}",
            amendment.getNaturalKey(), study.getAssignedIdentifier());
        templateDevelopmentService.deleteDevelopmentAmendmentOnly(study);
    }

    @Override
    public void verifyRemovable(final Amendment amendment) throws ResourceException {
        super.verifyRemovable(amendment);
        if (amendment == null) {
            String message = "Amendment is null";
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, message);
        } else if (amendment.equals(study.getAmendment()) || amendment.getReleasedDate()!=null) {
            String message = String.format("Amendment %s can be deleted if and only if it isn't released for the study %s",
                    amendment.getNaturalKey(), study.getAssignedIdentifier());
            log.error(message);
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, message);
        }
    }

    @Override
    protected void validateEntity(final Representation entity) throws ResourceException {
        super.validateEntity(entity);
    }


    @Override
    public void store(final Amendment amendment) {
        if (study.getDevelopmentAmendment() != null) {
            templateDevelopmentService.deleteDevelopmentAmendmentOnly(study);
        }
        study.setDevelopmentAmendment(amendment);
        studyService.save(study);
    }

    protected AmendmentXmlSerializer getAmendmentSerializer(Study study) {
        AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
        amendmentSerializer.setStudy(study);
        amendmentSerializer.setDevelopmentAmendment(false);
        return amendmentSerializer;
    }

    protected AmendmentXmlSerializer getDevelopmentAmendmentSerializer(Study study) {
        AmendmentXmlSerializer amendmentSerializer = (AmendmentXmlSerializer) getBeanFactory().getBean("amendmentXmlSerializer");
        amendmentSerializer.setStudy(study);
        amendmentSerializer.setDevelopmentAmendment(true);
        return amendmentSerializer;
    }

    @Override
    public AmendmentXmlSerializer getXmlSerializer() throws ResourceException {
        if (getRequestedObject().equals(study.getAmendment())) {
            return getAmendmentSerializer(study);
        } else {
            return getDevelopmentAmendmentSerializer(study);
        }
    }

    ////// CONFIGURATION

    @Required
    public void setStudyService(final StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAmendmentDao(AmendmentDao amendmentDao) {
        this.amendmentDao = amendmentDao;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Required
    public void setTemplateDevelopmentService(final TemplateDevelopmentService templateDevelopmentService) {
        this.templateDevelopmentService = templateDevelopmentService;
    }
}
