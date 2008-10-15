/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.SiteDao;
import edu.northwestern.bioinformatics.studycalendar.dao.StudyDao;
import edu.northwestern.bioinformatics.studycalendar.domain.Epoch;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.domain.StudySite;
import edu.northwestern.bioinformatics.studycalendar.service.AmendmentService;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.TemplateSkeletonCreatorImpl;
import gov.nih.nci.cabig.ccts.domain.*;
import gov.nih.nci.cabig.ctms.audit.dao.AuditHistoryRepository;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.StudyCreationException;
import gov.nih.nci.ccts.grid.studyconsumer.stubs.types.InvalidStudyException;
import gov.nih.nci.ccts.grid.studyconsumer.common.StudyConsumerI;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis.wsrf.properties.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Assumptions: 1. Site should already be existing in DB..
 *
 * @author <a href="mailto:saurabh.agrawal@semanticbits.com>Saurabh Agrawal</a>
 */

@Transactional(readOnly = false)
public class PSCStudyConsumer implements StudyConsumerI {

    private static final Log logger = LogFactory.getLog(PSCStudyConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";


    private SiteDao siteDao;

    private StudyService studyService;

    private StudyDao studyDao;

    private AuditHistoryRepository auditHistoryRepository;

    private String studyConsumerGridServiceUrl;

    private String rollbackTimeOut;


    private AmendmentService amendmentService;


    public void createStudy(final gov.nih.nci.cabig.ccts.domain.Study studyDto) throws RemoteException, InvalidStudyException,
            StudyCreationException {
        if (studyDto == null) {
            String message = "No Study message was found";
            throw getInvalidStudyException(message);
        }

        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);

        if (studyDao.getStudyIdByAssignedIdentifier(ccIdentifier) != null) {
            logger.debug("Already a study with the same Coordinating Center Identifier (" + ccIdentifier
                    + ") exists.Returning without processing the request.");
            return;
        }

        Study study = TemplateSkeletonCreatorImpl.createBase(studyDto.getShortTitleText());
        study.setAssignedIdentifier(ccIdentifier);
        study.setGridId(studyDto.getGridId());
        study.setLongTitle(studyDto.getLongTitleText());

        gov.nih.nci.cabig.ccts.domain.StudyOrganizationType[] studyOrganizationTypes = studyDto.getStudyOrganization();
        populateStudySite(study, studyOrganizationTypes);

        // now add epochs and arms to the planned calendar of study
        populateEpochsAndArms(studyDto, study);

        studyService.save(study);
        logger.info("Created the study :" + study.getId());

    }

    /**
     * does nothing as we are already  commiting Study message by default.
     *
     * @param study
     * @throws RemoteException
     * @throws InvalidStudyException
     */
    public void commit(final gov.nih.nci.cabig.ccts.domain.Study study) throws RemoteException, InvalidStudyException {
//        if (studyDto == null) {
//            throw new InvalidStudyException();
//        }
//        logger.info("commit called for study:long titlte-" + studyDto.getLongTitleText());
//        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);
//
//        try {
//            studyDao.commitInProgressStudy(ccIdentifier);
//        }
//
//        catch (Exception exp) {
//            logger.error("Exception while trying to commit the study", exp);
//            InvalidStudyException invalidStudyException = new InvalidStudyException();
//            invalidStudyException.setFaultReason("Exception while comitting study," + exp.getMessage());
//            invalidStudyException.setFaultString("Exception while comitting study," + exp.getMessage());
//            throw invalidStudyException;
//        }
    }

    public void rollback(final gov.nih.nci.cabig.ccts.domain.Study studyDto) throws RemoteException, InvalidStudyException {
        if (studyDto == null) {
            String message = "No Study message was found";
            throw getInvalidStudyException(message);
        }
        logger.info("rollback called for study:long titlte-" + studyDto.getLongTitleText());
        String ccIdentifier = findCoordinatingCenterIdentifier(studyDto);
        Study study = studyService.getStudyByAssignedIdentifier(ccIdentifier);

        if (study == null) {
            String message = "Exception while rollback study..no study found with given identifier:" + ccIdentifier;
            throw getInvalidStudyException(message);
        }
        //check if study was created by the grid service or not

        boolean checkIfEntityWasCreatedByGridService = auditHistoryRepository.checkIfEntityWasCreatedByUrl(study.getClass(), study.getId(), studyConsumerGridServiceUrl);

        if (!checkIfEntityWasCreatedByGridService) {
            logger.debug("Study was not created by the grid service url:" + studyConsumerGridServiceUrl + " so can not rollback this study:" + study.getId());
            return;
        }
        logger.info("Study (id:" + study.getId() + ") was created by the grid service url:" + studyConsumerGridServiceUrl);

        //check if this study was created one minute before or not
        Calendar calendar = Calendar.getInstance();

        Integer rollbackTime = 1;
        try {
            rollbackTime = Integer.parseInt(rollbackTimeOut);
        } catch (NumberFormatException e) {
            logger.error(String.format("error parsing value of rollback time out. Value of rollback time out %s must be integer.", rollbackTimeOut));
        }

        boolean checkIfStudyWasCreatedOneMinuteBeforeCurrentTime = auditHistoryRepository.
                checkIfEntityWasCreatedMinutesBeforeSpecificDate(study.getClass(), study.getId(), calendar, rollbackTime);
        try {
            if (checkIfStudyWasCreatedOneMinuteBeforeCurrentTime) {
                logger.info("Study was created one minute before the current time:" + calendar.getTime().toString() + " so deleting this study:" + study.getId());
                amendmentService.deleteDevelopmentAmendment(study);
            } else {
                logger.debug("Study was not created one minute before the current time:" + calendar.getTime().toString() + " so can not rollback this study:" + study.getId());
            }
        }
        catch (Exception expception) {
            String message = "Exception while rollback study," + expception.getMessage() + expception.getClass();
            expception.printStackTrace();
            throw getInvalidStudyException(message);
        }
    }

    public GetMultipleResourcePropertiesResponse getMultipleResourceProperties(final GetMultipleResourceProperties_Element getMultipleResourceProperties_element) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public GetResourcePropertyResponse getResourceProperty(final QName qName) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public QueryResourcePropertiesResponse queryResourceProperties(final QueryResourceProperties_Element queryResourceProperties_element) throws RemoteException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private void populateEpochsAndArms(final gov.nih.nci.cabig.ccts.domain.Study studyDto, final Study study) {
        EpochType[] epochTypes = studyDto.getEpoch();

        if (epochTypes != null) {
            for (int i = 0; i < epochTypes.length; i++) {
                EpochType epochType = epochTypes[i];
                if (epochType instanceof NonTreatmentEpochType) {
                    TemplateSkeletonCreatorImpl.addEpoch(study, i, Epoch.create(epochType.getName()));
                } else if (epochType instanceof TreatmentEpochType) {
                    TemplateSkeletonCreatorImpl.addEpoch(study, i,
                            createEpochForTreatmentEpochType((TreatmentEpochType) epochType));

                }

            }
        }

    }

    private Epoch createEpochForTreatmentEpochType(final TreatmentEpochType treatmentEpochType) {
        Epoch epoch = null;

        ArmType[] armTypes = treatmentEpochType.getArm();
        if (armTypes != null) {

            List<String> armNames = new ArrayList<String>();
            for (ArmType armType : armTypes) {
                armNames.add(armType.getName());
            }
            epoch = Epoch.create(treatmentEpochType.getName(), armNames.toArray(new String[0]));

        }
        return epoch;
    }

    /**
     * Populates study siste and returns it.
     *
     * @param study
     * @param studyOrganizationTypes
     * @throws InvalidStudyException
     */
    private void populateStudySite(final Study study, final gov.nih.nci.cabig.ccts.domain.StudyOrganizationType[] studyOrganizationTypes)
            throws StudyCreationException, InvalidStudyException {

        List<StudySite> studySites = new ArrayList<StudySite>();
        if (studyOrganizationTypes != null) {
            for (StudyOrganizationType studyOrganizationType : studyOrganizationTypes) {
                StudySite studySite = null;
                if (studyOrganizationType instanceof StudySiteType) {
                    studySite = new StudySite();
                    studySite.setSite(fetchSite(studyOrganizationType.getHealthcareSite(0).getNciInstituteCode()));
                    studySite.setStudy(study);
                    studySite.setGridId(studyOrganizationType.getGridId());
                }
                studySites.add(studySite);
            }
        }
        if (studySites.size() == 0 || ArrayUtils.isEmpty(studyOrganizationTypes)) {
            String message = "No sites is associated to this study" + study.getLongTitle();
            throw getStudyCreationException(message);

        }
        study.setStudySites(studySites);

    }

    /**
     * Fetches the site from the DB
     *
     * @param assignedIdentifier
     * @return
     */
    private Site fetchSite(final String assignedIdentifier) throws StudyCreationException {

        Site site = siteDao.getByAssignedIdentifier(assignedIdentifier);
        if (site == null) {

            String message = "No site exists  assignedIdentifier :" + assignedIdentifier;
            throw getStudyCreationException(message);
        }

        return site;
    }

    /**
     * This method will return the identifier specified by Coordinating center to this study.
     *
     * @param studyDto
     * @return
     * @throws InvalidStudyException
     */
    private String findCoordinatingCenterIdentifier(final gov.nih.nci.cabig.ccts.domain.Study studyDto) throws InvalidStudyException {
        String ccIdentifier = null;
        if (studyDto.getIdentifier() != null) {
            for (IdentifierType identifierType : studyDto.getIdentifier()) {
                if (identifierType instanceof OrganizationAssignedIdentifierType
                        && StringUtils.equals(identifierType.getType(), COORDINATING_CENTER_IDENTIFIER_TYPE)) {
                    ccIdentifier = identifierType.getValue();
                    break;
                }
            }
        }

        if (ccIdentifier == null) {
            String message = "no cc identifier for study:long titlte-" + studyDto.getLongTitleText();
            throw getInvalidStudyException(message);
        }
        return ccIdentifier;

    }


    private StudyCreationException getStudyCreationException(final String message) {
        StudyCreationException studyCreationException = new StudyCreationException();
        studyCreationException.setFaultString(message);
        studyCreationException.setFaultReason(message);
        logger.error(message);
        return studyCreationException;
    }

    private InvalidStudyException getInvalidStudyException(final String message) throws InvalidStudyException {
        logger.error(message);
        InvalidStudyException invalidStudyException = new InvalidStudyException();
        invalidStudyException.setFaultReason(message);
        invalidStudyException.setFaultString(message);
        throw invalidStudyException;
    }

    @Required
    public void setStudyConsumerGridServiceUrl(String studyConsumerGridServiceUrl) {
        this.studyConsumerGridServiceUrl = studyConsumerGridServiceUrl;
    }


    @Required
    public void setSiteDao(SiteDao siteDao) {
        this.siteDao = siteDao;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setStudyDao(StudyDao studyDao) {
        this.studyDao = studyDao;
    }

    @Required
    public void setAuditHistoryRepository(AuditHistoryRepository auditHistoryRepository) {
        this.auditHistoryRepository = auditHistoryRepository;
    }

    @Required
    public void setAmendmentService(final AmendmentService amendmentService) {
        this.amendmentService = amendmentService;
    }

    @Required
    public void setRollbackTimeOut(String rollbackTimeOut) {
        this.rollbackTimeOut = rollbackTimeOut;
    }
}
