/**
 * 
 */
package edu.northwestern.bioinformatics.studycalendar.grid;

import edu.northwestern.bioinformatics.studycalendar.api.ScheduledCalendarService;
import edu.northwestern.bioinformatics.studycalendar.domain.Arm;
import edu.northwestern.bioinformatics.studycalendar.domain.Subject;
import edu.northwestern.bioinformatics.studycalendar.domain.ScheduledCalendar;
import edu.northwestern.bioinformatics.studycalendar.domain.Site;
import edu.northwestern.bioinformatics.studycalendar.domain.Study;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.InvalidRegistration;
import gov.nih.nci.cabig.ctms.grid.ae.stubs.types.RegistrationFailed;
import gov.nih.nci.cagrid.metadata.security.ServiceSecurityMetadata;
import gov.nih.nci.ccts.grid.IdentifierType;
import gov.nih.nci.ccts.grid.ParticipantType;
import gov.nih.nci.ccts.grid.Registration;
import gov.nih.nci.ccts.grid.ScheduledTreatmentEpochType;
import gov.nih.nci.ccts.grid.common.RegistrationConsumer;

import java.rmi.RemoteException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author <a href="mailto:joshua.phillips@semanticbits.com>Joshua Phillips</a>
 * 
 */
public class PSCRegistrationConsumer implements RegistrationConsumer {

	private static final Log logger = LogFactory.getLog(PSCRegistrationConsumer.class);

	public static final String SERVICE_BEAN_NAME = "scheduledCalendarService";

	private static final String MRN_IDENTIFIER_TYPE = "MRN";

	private ApplicationContext ctx;

	public PSCRegistrationConsumer() {
		ctx = new ClassPathXmlApplicationContext(new String[] { "classpath:applicationContext.xml",
				"classpath:applicationContext-api.xml", "classpath:applicationContext-command.xml",
				"classpath:applicationContext-dao.xml", "classpath:applicationContext-security.xml",
				"classpath:applicationContext-service.xml", "classpath:applicationContext-spring.xml" });
	}

	/*
	 * (non-Javadoc)
	 * @see gov.nih.nci.cabig.ctms.common.RegistrationConsumer#createRegistration(gov.nih.nci.cabig.ctms.grid.RegistrationType)
	 */
	public Registration register(final Registration registration) throws RemoteException, InvalidRegistration,
			RegistrationFailed {

		Study study = new Study();
		Subject subject = new Subject();
		Site site = new Site();
		ScheduledCalendarService svc = (ScheduledCalendarService) ctx.getBean(SERVICE_BEAN_NAME);

		site.setGridId(registration.getStudySite().getGridId());
		study.setGridId(registration.getStudyRef().getGridId());

		ParticipantType partBean = registration.getSubject();
		subject.setGridId(partBean.getGridId());

		subject.setGender(partBean.getAdministrativeGenderCode());
		subject.setDateOfBirth(partBean.getBirthDate());
		subject.setFirstName(partBean.getFirstName());
		subject.setLastName(partBean.getLastName());

		String mrn = findIdentifierValue(partBean.getIdentifier(), MRN_IDENTIFIER_TYPE);
		subject.setPersonId(mrn);

		String registrationGridId = registration.getGridId();
		// Using the informed consent date as the calendar start date
		Date startDate = registration.getInformedConsentFormSignedDate();
		if (startDate == null) {
			startDate = new Date();
		}
		// retrieve Arm
		Arm arm = null;
		if (registration.getScheduledEpoch() != null
				&& registration.getScheduledEpoch() instanceof ScheduledTreatmentEpochType
				&& ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm() != null
				&& ((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm().getArm() != null) {
			arm = new Arm();
			arm.setGridId(((ScheduledTreatmentEpochType) registration.getScheduledEpoch()).getScheduledArm().getArm()
					.getGridId());
		}
		ScheduledCalendar scheduledCalendar = svc.assignSubject(study, subject, site, arm, startDate,
				registrationGridId);
		logger.debug("Created assignment " + scheduledCalendar.getId());
		return registration;
	}

	private String findIdentifierValue(final IdentifierType[] idents, final String desiredType)
			throws InvalidRegistration {
		String value = null;
		if (idents != null) {
			for (IdentifierType ident : idents) {
				if (ident.getType().equals(desiredType)) {
					value = ident.getValue();
					break;
				}
			}
		}
		if (value == null) {
			throw new InvalidRegistration();
		}
		return value;
	}

	public ServiceSecurityMetadata getServiceSecurityMetadata() throws RemoteException {
		throw new UnsupportedOperationException("Not implemented");
	}

}
