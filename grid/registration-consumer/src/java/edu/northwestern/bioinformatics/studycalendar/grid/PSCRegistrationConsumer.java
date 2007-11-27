/**
 *
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.dao.SubjectDao;
import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
import edu.northwestern.bioinformatics.studycalendar.domain.*;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import edu.northwestern.bioinformatics.studycalendar.service.StudyService;
import edu.northwestern.bioinformatics.studycalendar.service.SubjectService;
import edu.northwestern.bioinformatics.studycalendar.utils.accesscontrol.ApplicationSecurityManager;
import gov.nih.nci.ccts.grid.*;
import gov.nih.nci.ccts.grid.common.RegistrationConsumer;
import gov.nih.nci.ccts.grid.stubs.types.InvalidRegistrationException;
import gov.nih.nci.ccts.grid.stubs.types.RegistrationConsumptionException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 */
public class PSCRegistrationConsumer implements RegistrationConsumer {

    private static final org.apache.commons.logging.Log logger = LogFactory.getLog(PSCRegistrationConsumer.class);

    public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

    private static final String MRN_IDENTIFIER_TYPE = "MRN";

    private ApplicationContext ctx;

    private static final String COORDINATING_CENTER_IDENTIFIER_TYPE = "Coordinating Center Identifier";

    // private StudyDao studyDao;

    private StudyService studyService;

    private SubjectDao subjectDao;

    private UserDao userDao;

    private SubjectService subjectService;

    public PSCRegistrationConsumer() {
        ctx = new ClassPathXmlApplicationContext(new String[]{
                // "classpath:applicationContext.xml",
                "classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
                "classpath:applicationContext-dao.xml", "classpath:applicationContext-db.xml",
                "classpath:applicationContext-security.xml", "classpath:applicationContext-service.xml",
                "classpath:applicationContext-spring.xml"});

        // studyDao = getStudyDao();
        subjectDao = getSubjectDao();
        userDao = getUserDao();
        subjectService = getSubjectService();
        studyService = getStudyService();
    }

    public void commit(final Registration registration) throws RemoteException, InvalidRegistrationException {
        try {
            String mrn = findMedicalRecordNumber(registration.getParticipant());
            subjectDao.commitInProgressSubject(mrn);

        } catch (Exception exp) {
            InvalidRegistrationException e = new InvalidRegistrationException();
            e.setFaultReason("Error while comitting, " + exp.getMessage());
            e.setFaultString("Error while comitting, " + exp.getMessage());
            exp.printStackTrace();
            throw e;
        }
    }

    public void rollback(final Registration registration) throws RemoteException, InvalidRegistrationException {
        try {
            String mrn = findMedicalRecordNumber(registration.getParticipant());
            subjectDao.deleteInprogressSubject(mrn);

        } catch (Exception exp) {
            InvalidRegistrationException e = new InvalidRegistrationException();
            e.setFaultReason("Error while rollback, " + exp.getMessage());
            e.setFaultString("Error while rollback, " + exp.getMessage());
            exp.printStackTrace();
            throw e;

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
            RegistrationConsumptionException exp = new RegistrationConsumptionException();
            exp.setFaultString("Study identified by Coordinating Center Identifier '" + ccIdentifier
                    + "' doesn't exist");
            exp.setFaultReason("Study identified by Coordinating Center Identifier '" + ccIdentifier
                    + "' doesn't exist");
            throw exp;
        }

        String siteNCICode = registration.getStudySite().getHealthcareSite(0).getNciInstituteCode();
        StudySite studySite = findStudySite(study, siteNCICode);
        if (studySite == null) {
            RegistrationConsumptionException exp = new RegistrationConsumptionException();
            exp.setFaultReason("The study '" + study.getLongTitle()
                    + "', identified by Coordinating Center Identifier '" + ccIdentifier
                    + "' is not associated to a site identified by NCI code :'" + siteNCICode + "'");
            exp.setFaultString("The study '" + study.getLongTitle()
                    + "', identified by Coordinating Center Identifier '" + ccIdentifier
                    + "' is not associated to a site identified by NCI code :'" + siteNCICode + "'");
            throw exp;

        }
        String mrn = findMedicalRecordNumber(registration.getParticipant());
        Subject subject = fetchSubject(mrn);
        if (subject == null) {
            subject = createSubject(registration.getParticipant(), mrn);
            subjectDao.save(subject);
        } else {
            StudySubjectAssignment assignment = subjectDao.getAssignment(subject, study, studySite.getSite());
            if (assignment != null) {
                throw new IllegalArgumentException("Subject already assigned to this study. "
                        + "Use scheduleNextArm to change to the next arm.");
            }

        }
        // // retrieve Arm
        StudySegment studySegment = null;
        if (registration.getScheduledEpoch() != null
                && registration.getScheduledEpoch() instanceof ScheduledTreatmentEpochType
                && ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm() != null
                && ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm().getArm() != null) {
            studySegment = new StudySegment();
            studySegment.setName(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
                    .getArm().getName());
            studySegment.setGridId(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm()
                    .getArm().getGridId());
        }
        StudySegment loadedStudySegment = loadAndValidateStudySegmentInStudy(study, studySegment);

        String userName = ApplicationSecurityManager.getUser();
        // FIXME:Saurabh: check for correct implementation of userDao
        // User user = userDao.getByName(userName);

        String registrationGridId = registration.getGridId();
        // Using the informed consent date as the calendar start date
        Date startDate = registration.getInformedConsentFormSignedDate();
        if (startDate == null) {
            startDate = new Date();
        }

        StudySubjectAssignment newAssignment = subjectService.assignSubject(subject, studySite, loadedStudySegment,
                startDate, registrationGridId, null);

        ScheduledCalendar scheduledCalendar = newAssignment.getScheduledCalendar();
        logger.debug("Created assignment " + scheduledCalendar.getId());
        return registration;
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
            InvalidRegistrationException exp = new InvalidRegistrationException();
            exp.setFaultReason("In StudyRef-Identifiers, Coordinating Center Identifier is not available");
            exp.setFaultString("In StudyRef-Identifiers, Coordinating Center Identifier is not available");
            throw exp;
        }
        return ccIdentifier;

    }

    private String findIdentifierOfType(final IdentifierType[] idTypes, final String ofType) {
        if (idTypes == null) {
            return null;
        }
        for (IdentifierType idType : idTypes) {
            if (idType instanceof OrganizationAssignedIdentifierType && StringUtils.equals(idType.getType(), ofType)) {
                return idType.getValue();
            }
        }
        return null;
    }

    private Study fetchStudy(final String ccIdentifier) {
        Study study = studyService.getStudyNyAssignedIdentifier(ccIdentifier);

        return study;
    }

    private String findMedicalRecordNumber(final ParticipantType participantType) throws InvalidRegistrationException {
        String subjectIdentifier = findIdentifierOfType(participantType.getIdentifier(), MRN_IDENTIFIER_TYPE);

        if (subjectIdentifier == null) {
            logger.info("The subject has no identifiers.");
            InvalidRegistrationException exp = new InvalidRegistrationException();
            exp
                    .setFaultReason("There is no identifier associated to this subject, Medical Record Number(MRN) is needed to register this subject ");
            exp
                    .setFaultString("There is no identifier associated to this subject, Medical Record Number(MRN) is needed to register this subject");
            throw exp;
        }
        return subjectIdentifier;
    }

    Subject fetchSubject(final String mrn) {

        Subject subject = subjectDao.findSubjectByPersonId(mrn);

        return subject;
    }

    private StudySegment loadAndValidateStudySegmentInStudy(final Study study, final StudySegment requiredStudySegment) {
        for (Epoch epoch : study.getPlannedCalendar().getEpochs()) {
            List<StudySegment> studySegments = epoch.getStudySegments();
            for (StudySegment studySegment : studySegments) {
                if (studySegment.getName().equals(requiredStudySegment.getName())) {
                    return studySegment;
                }
            }

        }
        throw new IllegalArgumentException("Arm " + requiredStudySegment.getName() + " not part of template for study "
                + study.getGridId());
    }

    private Subject createSubject(final ParticipantType participantType, final String mrn) {
        Subject subject = new Subject();
        subject.setGridId(participantType.getGridId());

        subject.setGender(participantType.getAdministrativeGenderCode());
        subject.setDateOfBirth(participantType.getBirthDate());
        subject.setFirstName(participantType.getFirstName());
        subject.setLastName(participantType.getLastName());

        subject.setPersonId(mrn);
        subject.setLoadStatus(LoadStatus.INPROGRESS);
        return subject;
    }

    @Required
    public void setUserDao(final UserDao userDao) {
        this.userDao = userDao;
    }

    @Required
    public void setSubjectDao(final SubjectDao subjectDao) {
        this.subjectDao = subjectDao;
    }

    @Required
    public void setSubjectService(final SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    // public StudyDao getStudyDao() {
    // return (StudyDao) ctx.getBean("studyDao");
    // }

    public SubjectDao getSubjectDao() {
        return (SubjectDao) ctx.getBean("subjectDao");
    }

    public UserDao getUserDao() {
        return (UserDao) ctx.getBean("userDao");
    }

    public SubjectService getSubjectService() {
        return (SubjectService) ctx.getBean("subjectService");
    }

    public StudyService getStudyService() {
        return (StudyService) ctx.getBean("studyService");
    }

    @Required
    public void setStudyService(final StudyService studyService) {
        this.studyService = studyService;
    }
}
