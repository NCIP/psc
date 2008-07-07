/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.StudyCalendarSystemException;
import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.cabig.ctms.audit.dao.AuditHistoryRepository;
import gov.nih.nci.ccts.grid.*;
import gov.nih.nci.ccts.grid.common.RegistrationConsumer;
import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
@Transactional(readOnly = false)
public class PSCRegistrationConsumer implements RegistrationConsumer {

    private static final Log logger = LogFactory.getLog(PSCRegistrationConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String MRN_IDENTIFIER_TYPE = "MRN";


    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

    // private StudyDao studyDao;

    private StudyService studyService;

    private SubjectDao subjectDao;

    private UserDao userDao;

    private SubjectService subjectService;

    private AuditHistoryRepository auditHistoryRepository;

    private String registrationConsumerGridServiceUrl;


    /**
     * Does nothing as we are already  commiting Registraiton message by default.
     *
     * @param registration
     * @throws RemoteException
     * @throws InvalidRegistrationException
     */
    public void commit(final Registration registration) throws RemoteException, InvalidRegistrationException {
//        try {
//            String mrn = findMedicalRecordNumber(registration.getParticipant());
//            subjectDao.commitInProgressSubject(mrn);
//
//        } catch (Exception exp) {
//            InvalidRegistrationException e = new InvalidRegistrationException();
//            e.setFaultReason("Error while comitting, " + exp.getMessage());
//            e.setFaultString("Error while comitting, " + exp.getMessage());
//            exp.printStackTrace();
//            throw e;
//        }
    }

    public void rollback(final Registration registration) throws RemoteException, InvalidRegistrationException {

        String mrn = findMedicalRecordNumber(registration.getParticipant());
        Subject subject = fetchCommitedSubject(mrn);
        if (subject == null) {
            String message = "Exception while rollback subject..no subject found with given identifier:" + mrn;
            throw getInvalidRegistrationException(message);
        }
        try {
            //check if subject was created by the grid service or not

            boolean checkIfEntityWasCreatedByGridService = auditHistoryRepository.checkIfEntityWasCreatedByUrl(subject.getClass(), subject.getId(), registrationConsumerGridServiceUrl);

            if (!checkIfEntityWasCreatedByGridService) {
                logger.debug("Subject was not created by the grid service url:" + registrationConsumerGridServiceUrl + " so can not rollback this study:" + subject.getId());
                return;
            }
            logger.info("Subject (id:" + subject.getId() + ") was created by the grid service url:" + registrationConsumerGridServiceUrl);

            //check if this subject was created one minute before or not


            Calendar calendar = Calendar.getInstance();
            boolean checkIfSubjectWasCreatedOneMinuteBeforeCurrentTime = auditHistoryRepository.
                    checkIfEntityWasCreatedMinutesBeforeSpecificDate(subject.getClass(), subject.getId(), calendar, 1);
            if (!checkIfSubjectWasCreatedOneMinuteBeforeCurrentTime) {
                logger.debug("Subject was not created one minute before the current time:" + calendar.getTime().toString() + " so can not rollback this subject:" + subject.getId());
                return;

            }
            logger.info("Subject was created one minute before the current time:" + calendar.getTime().toString());
            List<StudySubjectAssignment> assignmentList = subject.getAssignments();
            if (assignmentList.size() > 1) {
                logger.info("Subject has more than one assignments so deleting the assignments only for the subject: " + subject.getId());
                subject.getAssignments().clear();
                subjectDao.save(subject);

            } else if (!assignmentList.isEmpty() && subject.getAssignments().size() == 1) {
                //this participant got created by the previous registration message. so delete it.
                logger.info("Subject has either only one assignments so deleting the subject: " + subject.getId());
                subjectDao.delete(subject);
            }
        } catch (Exception exception) {

            String message = "Error while rollback, " + exception.getMessage();
            throw getRegistrationConsumerException(message);

        }


    }

    /*
      * (non-Javadoc)
      * @see gov.nih.nci.cabig.ctms.common.RegistrationConsumer#createRegistration(gov.nih.nci.cabig.ctms.grid.RegistrationType)
      */
    public Registration register(final Registration registration) throws RemoteException, InvalidRegistrationException,
            RegistrationConsumptionException {

        String ccIdentifier = findCoordinatingCenterIdentifier(registration);
        Study study = fetchStudy(ccIdentifier);

        if (study == null) {
            String message = "Study identified by Coordinating Center Identifier '" + ccIdentifier + "' doesn't exist";
            throw getInvalidRegistrationException(message);
        }

        String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
        StudySite studySite = findStudySite(study, siteNCICode);
        if (studySite == null) {

            String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + ccIdentifier
                    + "' is not associated to a site identified by NCI code :'" + siteNCICode + "'";
            throw getInvalidRegistrationException(message);

        }
        String mrn = findMedicalRecordNumber(registration.getParticipant());

        Subject subject = fetchCommitedSubject(mrn);
        if (subject == null) {
            subject = createSubject(registration.getParticipant(), mrn);
            subjectDao.save(subject);
        } else {

            StudySubjectAssignment assignment = subjectDao.getAssignment(subject, study, studySite.getSite());
            if (assignment != null) {
                String message = "Subject already assigned to this study. Use scheduleNextArm to change to the next arm.";
                throw getInvalidRegistrationException(message);
            }

        }
        // // retrieve Arm
        StudySegment studySegment = null;
        StudySegment loadedStudySegment = null;
        if (registration.getScheduledEpoch() != null
                && registration.getScheduledEpoch() instanceof ScheduledTreatmentEpochType
                && ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm() != null
                && ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm().getArm() != null) {
            studySegment = new StudySegment();
            studySegment.setName(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
                    .getArm().getName());
            studySegment.setGridId(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
                    .getArm().getGridId());
            loadedStudySegment = loadAndValidateStudySegmentInStudy(study, studySegment);
        } else {
            try {
                loadedStudySegment = study.getPlannedCalendar().getEpochs().get(0).getStudySegments().get(0);
            } catch (Exception e) {
                String message = "The study '" + study.getLongTitle() + "', identified by Coordinating Center Identifier '" + ccIdentifier
                        + "' does not have any arm'";
                throw getInvalidRegistrationException(message);

            }
        }


        String userName = ApplicationSecurityManager.getUser();
        // FIXME:Saurabh: check for correct implementation of userDao
        // User user = userDao.getByName(userName);

        String registrationGridId = registration.getGridId();
        // Using the informed consent date as the calendar start date
        Date startDate = registration.getInformedConsentFormSignedDate();
        if (startDate == null) {
            startDate = new Date();
        }

        StudySubjectAssignment newAssignment = null;
        try {
            newAssignment = subjectService.assignSubject(subject, studySite, loadedStudySegment,
                    startDate, registrationGridId, null);
        } catch (StudyCalendarSystemException exp) {
            throw getRegistrationConsumerException(exp.getMessage());

        }

        ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
        logger.debug("Created assignment " + scheduledCalendar.getId());
        return registration;
    }

    private Subject fetchCommitedSubject(String mrn) {
        return subjectService.findSubjectByPersonId(mrn);

    }

    private StudySite findStudySite(final Study study, final String siteNCICode) {
        for (StudySite studySite : study.getStudySites()) {
            if (StringUtils.equals(studySite.getSite().getAssignedIdentifier(), siteNCICode)) {
                return studySite;
            }
        }
        return null;
    }

    /*
      * Finds the coordinating center identifier for the sutdy
      */
    private String findCoordinatingCenterIdentifier(final Registration registration)
            throws InvalidRegistrationException {
        String ccIdentifier = findIdentifierOfType(registration.getStudyRef().getIdentifier(),
                COORDINATING_CENTER_IDENTIFIER_TYPE);

        if (ccIdentifier == null) {
            String message = "In StudyRef-Identifiers, Coordinating Center Identifier is not available";
            throw getInvalidRegistrationException(message);
        }
        return ccIdentifier;

    }

    private InvalidRegistrationException getInvalidRegistrationException(String message) {
        InvalidRegistrationException invalidRegistrationException = new InvalidRegistrationException();

        invalidRegistrationException.setFaultReason(message);
        invalidRegistrationException.setFaultString(message);
        logger.error(message);
        return invalidRegistrationException;
    }

    private String findIdentifierOfType(final IdentifierType[] idTypes, final String ofType) {
        if (idTypes == null) {
            return null;
        }
        for (IdentifierType identifierType : idTypes) {
            if (identifierType instanceof OrganizationAssignedIdentifierType && StringUtils.equals(identifierType.getType(), ofType)) {
                return identifierType.getValue();
            }
        }
        return null;
    }

    private Study fetchStudy(final String ccIdentifier) {
        Study study = studyService.getStudyByAssignedIdentifier(ccIdentifier);

        return study;
    }

    private String findMedicalRecordNumber(final ParticipantType participantType) throws InvalidRegistrationException {
        String subjectIdentifier = findIdentifierOfType(participantType.getIdentifier(), MRN_IDENTIFIER_TYPE);

        if (subjectIdentifier == null) {

            String message = "There is no identifier associated to this subject, Medical Record Number(MRN) is needed to register this subject ";
            throw getInvalidRegistrationException(message);
        }
        return subjectIdentifier;
    }


    private StudySegment loadAndValidateStudySegmentInStudy(final Study study, final StudySegment requiredStudySegment) throws InvalidRegistrationException {
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            List<StudySegment> studySegments = epoch.getStudySegments();
            for (StudySegment studySegment : studySegments) {
                if (studySegment.getName().equals(requiredStudySegment.getName())) {
                    return studySegment;
                }
            }

        }
        String message = "Arm " + requiredStudySegment.getName() + " not part of template for study "
                + study.getGridId();
        throw getInvalidRegistrationException(message);
    }

    private Subject createSubject(final ParticipantType participantType, final String mrn) {
        Subject subject = new Subject();
        subject.setGridId(participantType.getGridId());
        if (Gender.getByCode(participantType.getAdministrativeGenderCode()) != null) {
            subject.setGender(Gender.getByCode(participantType.getAdministrativeGenderCode()));
        } else {
            subject.setGender(Gender.MALE);
        }
        subject.setDateOfBirth(participantType.getBirthDate());
        subject.setFirstName(participantType.getFirstName());
        subject.setLastName(participantType.getLastName());

        subject.setPersonId(mrn);
        return subject;
    }

    private RegistrationConsumptionException getRegistrationConsumerException(String message) {
        RegistrationConsumptionException registrationConsumptionException = new RegistrationConsumptionException();
        registrationConsumptionException.setFaultReason(message);
        registrationConsumptionException.setFaultString(message);
        logger.error(message);
        return registrationConsumptionException;
    }

    @Required
    public void setStudyService(StudyService studyService) {
        this.studyService = studyService;
    }

    @Required
    public void setSubjectDao(SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSubjectService(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @Required
    public void setAuditHistoryRepository(AuditHistoryRepository auditHistoryRepository) {
        this.auditHistoryRepository = auditHistoryRepository;
    }

    @Required
    public void setRegistrationConsumerGridServiceUrl(String registrationConsumerGridServiceUrl) {
        this.registrationConsumerGridServiceUrl = registrationConsumerGridServiceUrl;
    }
}
